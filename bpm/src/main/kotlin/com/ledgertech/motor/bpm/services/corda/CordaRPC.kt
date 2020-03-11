package com.ledgertech.motor.bpm.services.corda

import com.ledgertech.motor.bpm.config.CordaNodeConfiguration
import com.ledgertech.motor.bpm.config.CordaConfiguration
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.vault.ColumnPredicate
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

// Almost copy paste from corda-motor-ws project
@Component
class CordaRPC {
    @Autowired
    lateinit var cordaNetworkConfig: CordaConfiguration

    val connections: MutableMap<CordaX500Name, CordaRPCConnection?> = mutableMapOf()

    val disconnections: MutableMap<CordaNodeConfiguration, Instant> = mutableMapOf()

    // offset used when query for lost events
    val timeOffset: Long = 5
    val timeOffsetUnit = ChronoUnit.MINUTES

    companion object {
        val logger: Logger = LoggerFactory.getLogger(CordaRPC::class.java)
    }

    private fun initializeConnection(config: CordaNodeConfiguration): CordaRPCConnection? {
        val client = CordaRPCClient(NetworkHostAndPort(config.host, config.port.toInt()))
        var connection: CordaRPCConnection? = null
        try {
            logger.trace("Starting connection for $config")
            var gracefulReconnect: GracefulReconnect = GracefulReconnect(
                    Runnable {
                        logger.info("on disconnect from $config")
                        disconnections[config] = Instant.now()
                    },
                    Runnable {
                        logger.info("on reconnect at $config")
                        whenReconnect(config, connection)
                    })
            connection = client.start(config.user, config.password, gracefulReconnect)
        } catch (e: Exception) {
            logger.trace("Could not start connection for $config", e)
        }
        return connection
    }

    private fun whenReconnect(config: CordaNodeConfiguration, connection: CordaRPCConnection?) {
        // time moments are in UTC, hopefully the same as the ones from Corda nodes
        val t1: Instant? = disconnections.remove(config)?.minus(timeOffset, timeOffsetUnit)
        if (t1 != null) {
            val t2 = Instant.now().plus(timeOffset, timeOffsetUnit)
            val timeCondition: QueryCriteria.TimeCondition = QueryCriteria.TimeCondition(
                    QueryCriteria.TimeInstantType.RECORDED,
                    ColumnPredicate.Between(t1, t2))

            // query for all state types, only ones relevant to this ws will be treated
            val missedEvents : List<StateAndRef<LinearState>> = connection?.proxy?.vaultQueryByCriteria(
                    contractStateType = LinearState::class.java,
                    criteria = QueryCriteria.VaultQueryCriteria().withTimeCondition(timeCondition))
                    ?.states as List<StateAndRef<LinearState>>
            logger.info("there were ${missedEvents?.size} events lost on [$config] Corda node in [$timeCondition] interval")
        } else {
            logger.info("not found disconnection time for node: [$config]")
        }
    }

    fun ops(x500Name: String): CordaRPCOps? {
        val x500NameAsObject = CordaX500Name.parse(x500Name)
        if (!(connections.containsKey(x500NameAsObject) && connections[x500NameAsObject] != null)) {
            val config = cordaNetworkConfig.nodes.firstOrNull { it.name == x500Name }
            if (config != null) {
                connections[x500NameAsObject] = initializeConnection(config)
            }
        }
        return connections[x500NameAsObject]?.proxy
    }

    @PostConstruct
    fun initializeConnections() {
        logger.info("Initializing RPC connections to Corda nodes ...")
        cordaNetworkConfig.nodes.forEach {
            val x500 = CordaX500Name.parse(it.name)
            connections[x500] = initializeConnection(it)
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
}