package com.ledgertech.motor.corda.watcher

import com.ledgertech.motor.contracts.ClaimContract
import com.ledgertech.motor.contracts.FirstNotificationOfLossContract
import com.ledgertech.motor.contracts.PolicyContract
import com.ledgertech.motor.corda.messages.LinearId
import com.ledgertech.motor.corda.messages.StateChangedEvent
import com.ledgertech.motor.corda.messages.StateType
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.randomOrNull
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.vault.ColumnPredicate
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import rx.Subscription
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
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

// Almost copy paste from corda-motor-ws project
@Component
class CordaRPC {
    @Autowired
    lateinit var cordaNetworkConfig: CordaConfiguration

    @Autowired
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    val connections: MutableMap<CordaX500Name, CordaRPCConnection?> = mutableMapOf()
    val subscriptions: MutableMap<CordaX500Name, Subscription?> = mutableMapOf()

    val disconnections: MutableMap<CordaNodeConfiguration, Instant> = mutableMapOf()
    // this will contain all the processed states till now
    var processedStates: MutableList<StateAndRef<ContractState>> = mutableListOf()

    // offset used when query for lost events
    val timeOffset: Long = 5
    val timeOffsetUnit = ChronoUnit.MINUTES

    companion object {
        val logger: Logger = LoggerFactory.getLogger(CordaRPC::class.java)
    }

    private fun initializeConnection(x500: CordaX500Name, config: CordaNodeConfiguration): CordaRPCConnection? {
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
                        whenReconnect(x500, config, connection)
                    })
            connection = client.start(config.user, config.password, gracefulReconnect)
        } catch (e: Exception) {
            logger.trace("Could not start connection for $config", e)
        }
        return connection
    }

    private fun whenReconnect(x500: CordaX500Name, config: CordaNodeConfiguration, connection: CordaRPCConnection?) {
        // time moments are in UTC, hopefully the same as the ones from Corda nodes
        val t1: Instant? = disconnections.remove(config)?.minus(timeOffset, timeOffsetUnit)
        if (t1!=null){
            val t2 = Instant.now().plus(timeOffset, timeOffsetUnit)
            val timeCondition:QueryCriteria.TimeCondition = QueryCriteria.TimeCondition(
                    QueryCriteria.TimeInstantType.RECORDED,
                    ColumnPredicate.Between(t1, t2))

            // query for all state types, only ones relevant to this ws will be treated
            val missedEvents : List<StateAndRef<LinearState>> = connection?.proxy?.vaultQueryByCriteria(
                    contractStateType = LinearState::class.java,
                    criteria = QueryCriteria.VaultQueryCriteria().withTimeCondition(timeCondition))
                    ?.states as List<StateAndRef<LinearState>>
            logger.info("there were ${missedEvents?.size} events lost on [$config] Corda node in [$timeCondition] interval")

            missedEvents?.forEach{it -> treatEvent(x500, it) }

        } else {
            logger.info("not found disconnection time for node: [$config]")
        }
    }

    private fun initializeSubscription(x500: CordaX500Name, connection: CordaRPCConnection?): Subscription? {
        logger.debug("Starting to register subscription for Corda node ...")
        val dataFeed = connection?.proxy?.vaultTrackByCriteria(
                contractStateType = LinearState::class.java,
                criteria = QueryCriteria.VaultQueryCriteria()
                        .withContractStateTypes(setOf(
                                FirstNotificationOfLossContract.State::class.java,
                                PolicyContract.State::class.java,
                                ClaimContract.State::class.java
                        ))
        )

        dataFeed?.snapshot?.statesMetadata
        val subscription = dataFeed?.updates?.subscribe { updates ->
            logger.debug("Handling {} vault updates ...", updates.produced.size)

            updates.produced
                    .forEach {
                        treatEvent(x500, it)
                    }
        }
        if (subscription == null) logger.debug("Failed to register subscription ...")
        return subscription
    }

    private fun treatEvent(x500: CordaX500Name, t: StateAndRef<ContractState>) {
        val it = t.state.data as LinearState

        if (!processedStates.contains(t)) {
            val stateType = when (it) {
                is FirstNotificationOfLossContract.State -> "FNOL"
                is PolicyContract.State -> "POLICY"
                is ClaimContract.State -> "CLAIM"
                else -> null
            }
            if (stateType != null) {
                val message = StateChangedEventImpl(
                        node = x500.toString(),
                        stateType = StateType(stateType),
                        stateLinearId = LinearId(id = UUID.randomUUID()),
                        stateId = t.ref.toString(),
                        recordedTime = Instant.now()
                )

                applicationEventPublisher.publishEvent(message)
            } else {
                logger.info("event $it is ignored as having a not-relevant type")
            }

            processedStates.add(t)
        } else {
            logger.debug("event $it was already treated")
        }
    }

    /**
     * Retrieve a CordaX500Name to be used when an AccountInfo is created.
     */
    fun nodeForAccount(): String? {
        // TODO: Use an algorithm for balancing the accounts between the available Customer Corda nodes
        return connections.toList().filter { it.second != null }.map { it.first.toString() }.randomOrNull()
    }

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

   // @PostConstruct
    fun initializeConnections() {
        logger.info("Initializing RPC connections to Corda nodes ...")
        cordaNetworkConfig.nodes.forEach {
            val x500 = CordaX500Name.parse(it.name)
            connections[x500] = initializeConnection(x500, it)
            logger.info("Trying to subscribe for vault events to {}", x500)
            subscriptions[x500] = initializeSubscription(x500, connections[x500])
        }

        logger.info("Connections started ...")
    }

    @PreDestroy
    fun closeConnections() {
        subscriptions.forEach { (x500Name, subscription) ->
            subscription?.let {
                logger.info("Unsubscribed from {}", x500Name )
                it.unsubscribe()
            }
        }

        connections.forEach { (x500Name, connection) -> run {
            connection?.let {
                logger.info("Closing RPC connection to {}", x500Name)
                it.notifyServerAndClose()
            }
        }
        }
    }
}
