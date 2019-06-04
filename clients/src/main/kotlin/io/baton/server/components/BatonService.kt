package io.baton.server.components

import com.github.manosbatsis.corbeans.spring.boot.corda.rpc.NodeRpcConnection
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeServiceImpl
import io.baton.SendChat
import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory


class BatonService(
        nodeRpcConnection: NodeRpcConnection
) : CordaNodeServiceImpl(nodeRpcConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodeServiceImpl::class.java)
    }
    /** Send a Chat! */
    fun sendChat(to: String, message: String): Unit {
        val proxy = this.nodeRpcConnection.proxy

        val matches = proxy.partiesFromName(to, exactMatch = true)
        logger.debug("sendChat, peers: {}", this.peers())
        logger.debug("sendChat, peer names: {}", this.peerNames())
        logger.debug("sendChat, target: {}, matches: {}", to, matches)

        val to: Party = when {
            matches.isEmpty() -> throw IllegalArgumentException("Target string \"$to\" doesn't match any nodes on the network.")
            matches.size > 1 -> throw IllegalArgumentException("Target string \"$to\"  matches multiple nodes on the network.")
            else -> matches.single()
        }
        // Start the flow, block and wait for the response.
        return proxy.startFlowDynamic(SendChat::class.java, to, message).returnValue.getOrThrow()
    }

}