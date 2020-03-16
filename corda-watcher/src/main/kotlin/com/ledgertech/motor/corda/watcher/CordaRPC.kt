package com.ledgertech.motor.corda.watcher

import com.ledgertech.motor.corda.messages.LinearId
import com.ledgertech.motor.corda.messages.StateChangedEvent
import com.ledgertech.motor.corda.messages.StateType
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

data class StateChangedEventImpl(
        override val node: String,
        override val stateType: StateType,
        override val stateLinearId: LinearId,
        override val stateId: String,
        override val recordedTime: Instant
): StateChangedEvent {
    override val eventType: String = "STATE_UPDATE"
    override val payload: Any? = null
}

@Component
class CordaRPC {
    @Autowired
    lateinit var cordaNetworkConfig: CordaConfiguration

    val connections: MutableMap<CordaX500Name, CordaRPCConnection?> = mutableMapOf()

    companion object {
        val logger: Logger = LoggerFactory.getLogger(CordaRPC::class.java)
    }

    @Synchronized
    fun ops(x500Name: String): CordaRPCOps? {
        val x500NameAsObject = CordaX500Name.parse(x500Name)

        if (!(connections.containsKey(x500NameAsObject) && connections[x500NameAsObject] != null)) {
            val config = cordaNetworkConfig.nodes.firstOrNull { it.name == x500Name }

            if (config != null) {
                connections[x500NameAsObject] = initializeConnection(x500NameAsObject, config)
            }
        }

        return connections[x500NameAsObject]?.proxy
    }

    @PostConstruct
    fun initializeConnections() {
        logger.info("Initializing RPC connections to Corda nodes ...")
        cordaNetworkConfig.nodes.forEach {
            val x500 = CordaX500Name.parse(it.name)
            connections[x500] = initializeConnection(x500, it)
        }

        logger.info("Connections started ...")
    }

    @PreDestroy
    fun closeConnections() {
        connections.forEach { (x500Name, connection) ->
            run {
                connection?.let {
                    logger.info("Closing RPC connection to {}", x500Name)
                    it.notifyServerAndClose()
                }
            }
        }
    }

    private fun initializeConnection(x500: CordaX500Name, config: CordaNodeConfiguration): CordaRPCConnection? {
        val client = CordaRPCClient(NetworkHostAndPort(config.host, config.port.toInt()))
        var connection: CordaRPCConnection? = null
        try {
            logger.trace("Starting connection for $config")
            connection = client.start(config.user, config.password)
            logger.trace("Connection created for $config")
        } catch (e: Exception) {
            logger.trace("Could not start connection for $config", e)
        }

        return connection
    }
}
