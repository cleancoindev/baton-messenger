package io.baton.webserver.components

import com.github.manosbatsis.corbeans.spring.boot.corda.rpc.NodeRpcConnection
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeServiceImpl
import io.baton.SendChat
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory


class BatonService(
        nodeRpcConnection: NodeRpcConnection
) : CordaNodeServiceImpl(nodeRpcConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodeServiceImpl::class.java)
    }
    /** Send a Chat! */
    fun sendChat(target: String, message: String): SignedTransaction {
        val proxy = this.nodeRpcConnection.proxy
        // Look-up the 'target'.
        val matches = proxy.partiesFromName(target, exactMatch = true)
        logger.debug("sendChat, peers: {}", this.peers())
        logger.debug("sendChat, peer names: {}", this.peerNames())
        logger.debug("sendChat, target: {}, matches: {}", target, matches)
        // We only want one result!
        val to: Party = when {
            matches.isEmpty() -> throw IllegalArgumentException("Target string \"$target\" doesn't match any nodes on the network.")
            matches.size > 1 -> throw IllegalArgumentException("Target string \"$target\"  matches multiple nodes on the network.")
            else -> matches.single()
        }
        // Start the flow, block and wait for the response.
        return proxy.startFlowDynamic(SendChat::class.java, to, message).returnValue.getOrThrow()
    }

}