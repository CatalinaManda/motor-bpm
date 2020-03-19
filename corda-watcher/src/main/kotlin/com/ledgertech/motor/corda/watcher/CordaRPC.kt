package com.ledgertech.motor.corda.watcher

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCClientConfiguration
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PreDestroy

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

        if (!connections.containsKey(x500NameAsObject)) {
            val config = cordaNetworkConfig.nodes.firstOrNull { it.name == x500Name }

            if (config != null) {
                var conn = initializeConnection(x500NameAsObject, config)

                if (conn != null) {
                    connections[x500NameAsObject] = conn
                }
            }
        }

        return connections[x500NameAsObject]?.proxy
    }

    @Synchronized
    fun connection(x500Name: String): CordaRPCConnection? {
        val x500NameAsObject = CordaX500Name.parse(x500Name)
        val config = cordaNetworkConfig.nodes.firstOrNull { it.name == x500Name }
        var conn: CordaRPCConnection? = null

        if (config != null) {
            conn = initializeConnection(x500NameAsObject, config, false)
        }

        return conn
    }

    //@PostConstruct
    fun initializeConnections() {
        logger.info("Initializing RPC connections to Corda nodes ...")

        cordaNetworkConfig.nodes.forEach {
            val x500 = CordaX500Name.parse(it.name)
            val conn = initializeConnection(x500, it)

            if (conn != null) {
                connections[x500] = conn
            }
        }

        logger.info("Connections initialized.")
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

    private fun initializeConnection(x500: CordaX500Name, config: CordaNodeConfiguration, enableReconnect: Boolean = true): CordaRPCConnection? {
        val client = CordaRPCClient(hostAndPort = NetworkHostAndPort(config.host, config.port.toInt()),
                                    configuration = CordaRPCClientConfiguration(maxReconnectAttempts = 3,
                                            connectionRetryInterval = Duration.ofSeconds(5),
                                            connectionMaxRetryInterval = Duration.ofSeconds(20)))
        var connection: CordaRPCConnection? = null

        try {
            logger.debug("Starting connection for $config")
            connection = client.start(config.user, config.password,
                    if (enableReconnect) GracefulReconnect(
                            Runnable {
                                logger.debug("{}: disconnected", x500)
                            },
                            Runnable {
                                logger.debug("{}: reconnected", x500)
                            })
                    else null)

            if (connection != null) {
                logger.debug("{}: connection created", x500)
            } else {
                logger.debug("{}: could not start connection", x500)
            }
        } catch (e: Exception) {
            logger.debug("{}: could not start connection", x500, e)
        }

        return connection
    }
}
