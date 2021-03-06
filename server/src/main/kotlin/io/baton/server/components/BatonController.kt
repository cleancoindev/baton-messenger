/**
 *   Copyright 2020, Dapps Incorporated.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


package io.baton.server.components

import io.baton.contracts.Chat
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import sun.security.timestamp.TSResponse
import org.springframework.web.bind.annotation.PostMapping
import com.github.manosbatsis.corbeans.spring.boot.corda.config.NodeParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import java.util.*
import javax.annotation.PostConstruct
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.CrossOrigin



/**
 * Baton Messenger API Endpoints
 */

@CrossOrigin(origins = ["https://dapps.ngrok.io", "https://dsoa.network"])
@RestController
@RequestMapping("/api/{nodeName}")
class RestController() {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }


    protected lateinit var defaultNodeName: String

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    protected lateinit var services: Map<String, BatonService>

    @PostConstruct
    fun postConstruct() {
        // if single node config, use the only node name as default, else reserve explicitly for cordform
        defaultNodeName = if (services.keys.size == 1) services.keys.first() else NodeParams.NODENAME_CORDFORM
        logger.debug("Auto-configured RESTful services for Corda nodes:: {}, default node: {}", services.keys, defaultNodeName)
    }

    /**
     * Handle both "api/sendMessage" and "api/message/{nodeName}" by using `cordform` as the default
     * node name to support optional dedicated server per node when using `runnodes`.
     */
    fun getService(optionalNodeName: Optional<String>): BatonService {
        val nodeName = if (optionalNodeName.isPresent) optionalNodeName.get() else defaultNodeName
        return this.services.get("${nodeName}NodeService") ?: throw IllegalArgumentException("Node not found: $nodeName")
    }


    private inline fun <reified U : ContractState> getState(
            services: ServiceHub,
            block: (generalCriteria: QueryCriteria.VaultQueryCriteria) -> QueryCriteria
    ): List<StateAndRef<U>> {
        val query = builder {
            val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
            block(generalCriteria)
        }
        val result = services.vaultService.queryBy<U>(query)
        return result.states
    }


    /** Maps an Chat to a JSON object. */

    private fun Chat.Message.toJson(): Map<String, String> {
        return kotlin.collections.mapOf(
                "id" to id.toString(),
                "body" to body,
                "to" to to.name.organisation,
                "from" to from.name.organisation,
                "sentReceipt" to sentReceipt.toString(),
                "deliveredReceipt" to deliveredReceipt.toString(),
                "fromMe" to fromMe.toString(),
                "time" to time.toString())
    }


    /** Returns a list of existing Messages. */

    @CrossOrigin(origins = ["https://dapps.ngrok.io", "https://dsoa.network"])
    @GetMapping(value = "/getMessages", produces = arrayOf("application/json"))
    @ApiOperation(value = "Get Baton Messages")
    fun getMessages(@PathVariable nodeName: Optional<String>): List<Map<String, String>> {
        val messageStateAndRefs = this.getService(nodeName).proxy().vaultQueryBy<Chat.Message>().states
        val messageStates = messageStateAndRefs.map { it.state.data }
        return messageStates.map { it.toJson() }
    }


    /** Get Messages by UserId */

    @CrossOrigin(origins = ["https://dapps.ngrok.io", "https://dsoa.network"])
    @GetMapping(value = "/getMessages/userId", produces = arrayOf("application/json"))
    @ApiOperation(value = "Get Baton Messages by userId")
    fun getMessagesByUserId(@PathVariable nodeName: Optional<String>): List<Map<String, String>> {
        val messageStateAndRefs = this.getService(nodeName).proxy().vaultQueryBy<Chat.Message>().states
        val messageStates = messageStateAndRefs.map { it.state.data }
        return messageStates.map { it.toJson() }
    }



    /** Returns a list of received Messages. */

    @CrossOrigin(origins = ["https://dapps.ngrok.io", "https://dsoa.network"])
    @GetMapping(value = "/getReceivedMessages", produces = arrayOf("application/json"))
    @ApiOperation(value = "Get Received Baton Messages")
    fun getRecievedMessages(@PathVariable nodeName: Optional<String>): List<Map<String, String>> {
        val messageStateAndRefs = this.getService(nodeName).proxy().vaultQueryBy<Chat.Message>().states
        val messageStates = messageStateAndRefs.map { it.state.data }
        return messageStates.map { it.toJson() }
    }



    /** Returns a list of Sent Messages. */

    @CrossOrigin(origins = ["https://dapps.ngrok.io", "https://dsoa.network"])
    @GetMapping(value = "/getSentMessages", produces = arrayOf("application/json"))
    @ApiOperation(value = "Get Sent Baton Messages")
    fun getSentMessages(@PathVariable nodeName: Optional<String>): List<Map<String, String>> {
        val messageStateAndRefs = this.getService(nodeName).proxy().vaultQueryBy<Chat.Message>().states
        val messageStates = messageStateAndRefs.map { it.state.data }
        return messageStates.map { it.toJson() }
    }


    /** Send Message*/


    @CrossOrigin(origins = ["https://dapps.ngrok.io", "https://dsoa.network"])
    @PostMapping(value = "/sendMessage")
    @ApiOperation(value = "Send a message to the target party")
    fun sendMessage(@PathVariable nodeName: Optional<String>,
                 @ApiParam(value = "The target party for the message")
                 @RequestParam(required = true) to: String,
                 @ApiParam(value = "The user Id for the message")
                 @RequestParam(required = true) userId: String,
                 @ApiParam(value = "The message text")
                 @RequestParam("body") body: String): ResponseEntity<Any?> {

        if (body== null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameter 'body' can not be null.\n")
        }

        if (to == null) {
            return ResponseEntity.status(TSResponse.BAD_REQUEST).body("Query parameter 'recipient' missing or has wrong format.\n")
        }

        if (userId == null) {
            return ResponseEntity.status(TSResponse.BAD_REQUEST).body("Query parameter 'userId' missing or has wrong format.\n")
        }


        val (status, message) = try {

            val result = getService(nodeName).sendMessage(to, userId, body)

            HttpStatus.CREATED to mapOf<String, String>(
                    "body" to "$body",
                    "to" to "$to",
                    "userId" to "$userId"
            )

        } catch (e: Exception) {
            logger.error("Error sending message to ${to}", e)
            e.printStackTrace()
            HttpStatus.BAD_REQUEST to e.message
        }
        return ResponseEntity<Any?>(message, status)
    }
    /*

    /** Send UPI Payment */

    @CrossOrigin(origins = ["https://dapps.ngrok.io", "https://dsoa.network"])
    @PostMapping(value = "/pay")
    fun sendPayment(@RequestParam("pa") pa: String,
                    @RequestParam("pn") pn: String,
                    @RequestParam("mc") mc: String,
                    @RequestParam("tid") tid: String,
                    @RequestParam("tr") tr: String,
                    @RequestParam("tn") tn: String,
                    @RequestParam("am") am: String,
                    @RequestParam("mam") mam: String,
                    @RequestParam("cu") cu: String,
                    @RequestParam("url") url : String): ResponseEntity<Any?> {

        if (tid == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameter 'tid' can not be null.\n")
        }

        if (pn == null) {
            return ResponseEntity.status(TSResponse.BAD_REQUEST).body("Query parameter 'pn' missing or has wrong format.\n")
        }

        val counterparty = CordaX500Name.parse(pn)


        val pn = proxy.wellKnownPartyFromX500Name(counterparty)
                ?: return ResponseEntity.status(TSResponse.BAD_REQUEST).body("Party named $pn cannot be found.\n")

        val (status, message) = try {


            val flowHandle = proxy.startFlowDynamic(SendPaymentFlow.InitiatePaymentRequest::class.java, pa, pn, mc, tid, tr, tn, am, mam, cu, url)

            val result = flowHandle.use { it.returnValue.getOrThrow() }

            HttpStatus.CREATED to "Payment sent."

        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }
        logger.info(message)
        return ResponseEntity<Any?>(message, status)
    }


/** Send Proxy Re-encryption Policy */

@CrossOrigin(origins = ["https://dapps.ngrok.io", "https://dsoa.network"])
@PostMapping(value = "/policy")
fun sendPolicy(@RequestParam("alice") alice: String,
                @RequestParam("enrico") enrico: String,
                @RequestParam("bob") bob: String,
                @RequestParam("policyName") policyName: String,
                @RequestParam("policyExpirationDate") policyExpirationDate: String,
                @RequestParam("policyPassword") policyPassword: String,
                @RequestParam("policyId") policyId: String): ResponseEntity<Any?> {

    if (policyId == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameter 'tid' can not be null.\n")
    }

    if (bob == null) {
        return ResponseEntity.status(TSResponse.BAD_REQUEST).body("Query parameter 'pn' missing or has wrong format.\n")
    }

    val counterparty = CordaX500Name.parse(bob)

    val bob = proxy.wellKnownPartyFromX500Name(counterparty)
            ?: return ResponseEntity.status(TSResponse.BAD_REQUEST).body("Party named $bob cannot be found.\n")

    val (status, message) = try {


        val flowHandle = proxy.startFlowDynamic(SendPolicyFlow.InitiatePolicyRequest::class.java, alice, enrico, bob, policyName, policyExpirationDate, policyPassword, policyId)

        val result = flowHandle.use { it.returnValue.getOrThrow() }

        HttpStatus.CREATED to "Payment sent."

    } catch (e: Exception) {
        HttpStatus.BAD_REQUEST to e.message
    }
    logger.info(message)
    return ResponseEntity<Any?>(message, status)
}

*/
}
