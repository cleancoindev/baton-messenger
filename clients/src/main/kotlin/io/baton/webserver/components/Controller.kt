/**
 *   Copyright 2019, Dapps Incorporated.
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


package io.baton.webserver.components

import io.baton.SendChat
import io.baton.SendPaymentFlow
import io.baton.SendPolicyFlow
import io.baton.contracts.Chat
import io.baton.user.User
import io.baton.user.UserRepository
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import sun.security.timestamp.TSResponse
import java.time.LocalDateTime
import java.time.ZoneId
import org.springframework.web.bind.annotation.PostMapping
import com.github.manosbatsis.corbeans.spring.boot.corda.config.NodeParams
import io.baton.webserver.NodeRPCConnection
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*
import javax.annotation.PostConstruct

/**
 * Baton Messenger API Endpoints
 */

@RestController
@RequestMapping("/api")
class RestController(
        private val rpc: NodeRPCConnection,
        val repository: UserRepository) {


    val authorization = SecurityContextHolder.getContext().authentication

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
     * Handle both "api/sendChat" and "api/sendChat/{nodeName}" by using `cordform` as the default
     * node name to support optional dedicated webserver per node when using `runnodes`.
     */
    fun getService(optionalNodeName: Optional<String>): BatonService {
        val nodeName = if (optionalNodeName.isPresent) optionalNodeName.get() else defaultNodeName
        return this.services.get("${nodeName}NodeService") ?: throw IllegalArgumentException("Node not found: $nodeName")
    }


    private val me = rpc.proxy.nodeInfo().legalIdentities.first().name

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
                "messageId" to messageId.toString(),
                "message" to message,
                "to" to to.name.organisation,
                "from" to from.name.organisation,
                "sentReceipt" to sentReceipt.toString(),
                "deliveredReceipt" to deliveredReceipt.toString(),
                "fromMe" to fromMe.toString(),
                "time" to time.toString())
    }



    /** Returns the node's name. */
    @GetMapping(value = "/me", produces = arrayOf("text/plain"))
    private fun me() = me.toString()

    @GetMapping(value = "/status", produces = arrayOf("text/plain"))
    private fun status() = "200"

    @GetMapping(value = "/servertime", produces = arrayOf("text/plain"))
    private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping(value = "/addresses", produces = arrayOf("text/plain"))
    private fun addresses() = proxy.nodeInfo().addresses.toString()

    @GetMapping(value = "/identities", produces = arrayOf("text/plain"))
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @GetMapping(value = "/platformversion", produces = arrayOf("text/plain"))
    private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()

    @GetMapping(value = "/peers", produces = arrayOf("text/plain"))
    private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @GetMapping(value = "/notaries", produces = arrayOf("text/plain"))
    private fun notaries() = proxy.notaryIdentities().toString()

    @GetMapping(value = "/flows", produces = arrayOf("text/plain"))
    private fun flows() = proxy.registeredFlows().toString()

    /** Find All Users. */
    @GetMapping
    fun findAll() = repository.findAll()


    /** Add a new User */

    @PostMapping
    fun addUser(@RequestBody user: User) = repository.save(user)


    /** Update a User */

    @PutMapping(value = "/{id}")
    fun updateUser(@PathVariable userId: Long, @RequestBody user: User) {
        assert(user.userId == userId)
        repository.save(user)
    }

    /** Remove User */

    @DeleteMapping(value = "/{id}")
    fun removeUser(@PathVariable userId: String) = repository.deleteById(userId)


    /** Get User by Id */

    @GetMapping(value = "/{id}")
    fun getById(@PathVariable userId:String) = repository.findById(userId)


    private val proxy = rpc.proxy


    /** Returns a list of existing Messages. */


    @GetMapping(value = "/getMessages", produces = arrayOf("application/json"))
    fun getMessages(): List<Map<String, String>> {
        val messageStateAndRefs = rpc.proxy.vaultQueryBy<Chat.Message>().states
        val messageStates = messageStateAndRefs.map { it.state.data }
        return messageStates.map { it.toJson() }
    }


    /** Returns a list of received Messages. */


    @GetMapping(value = "/getReceivedMessages", produces = arrayOf("application/json"))
    fun getRecievedMessages(): List<Map<String, String>> {
        val messageStateAndRefs = rpc.proxy.vaultQueryBy<Chat.Message>().states
        val messageStates = messageStateAndRefs.map { it.state.data }
        return messageStates.map { it.toJson() }
    }

    /** Returns a list of Sent Messages. */


    @GetMapping(value = "/getSentMessages", produces = arrayOf("application/json"))
    fun getSentMessages(): List<Map<String, String>> {
        val messageStateAndRefs = rpc.proxy.vaultQueryBy<Chat.Message>().states
        val messageStates = messageStateAndRefs.map { it.state.data }
        return messageStates.map { it.toJson() }
    }


    /** Send Chat */



    @PostMapping(value = "/sendChat")
    @ApiOperation(value = "Send a message to the target party")
    fun sendChat(@PathVariable nodeName: Optional<String>,
                 @ApiParam(value = "The target party for the message")
                 @RequestParam(required = true) to: String,
                 @RequestParam("message") message: String): ResponseEntity<Any?> {

        if (message == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Query parameter 'message' can not be null.\n")
        }

        if (to == null) {
            return ResponseEntity.status(TSResponse.BAD_REQUEST).body("Query parameter 'recipient' missing or has wrong format.\n")
        }

        val (status, message) = try {

            val result = getService(nodeName).sendChat(to, message)

            HttpStatus.CREATED to mapOf<String, String>(
                    "message" to "$message",
                    "to" to "$to",
                    "transactionId" to "${result.tx.id}"
                    )

        } catch (e: Exception) {
            logger.error("Error sending message to ${to}", e)
            e.printStackTrace()
            HttpStatus.BAD_REQUEST to e.message
        }
        return ResponseEntity<Any?>(message, status)
    }



    /** Send UPI Payment */


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


@PostMapping(value = "/policy")
fun sendPayment(@RequestParam("alice") alice: String,
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
}
