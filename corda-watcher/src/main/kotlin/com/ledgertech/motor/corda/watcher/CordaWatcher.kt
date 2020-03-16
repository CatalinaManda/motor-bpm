package com.ledgertech.motor.corda.watcher

import com.ledgertech.motor.contracts.ClaimContract
import com.ledgertech.motor.contracts.FirstNotificationOfLossContract
import com.ledgertech.motor.contracts.PolicyContract
import com.ledgertech.motor.corda.messages.LinearId
import com.ledgertech.motor.corda.messages.StateType
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.vault.ColumnPredicate
import net.corda.core.node.services.vault.QueryCriteria
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.util.*

@Scope("prototype")
class CordaWatcher(val x500Name: String) {
    @Autowired lateinit var rabbitTemplate: RabbitTemplate
    @Autowired lateinit var rpc: CordaRPC

    init {
        logger.info("CREATED")
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(CordaWatcher::class.java)
    }

    private var lastTimestamp = Instant.MIN

    @Scheduled(fixedRateString ="#{cordaConfiguration.watcher.fixedRate}", initialDelay=1000)
    fun lookForEvents() {
        logger.info("{}: Looking for states changes", this.x500Name)

        var conn = this.rpc.ops(this.x500Name)
        val timeCondition: QueryCriteria.TimeCondition = QueryCriteria.TimeCondition(
                QueryCriteria.TimeInstantType.RECORDED,
                ColumnPredicate.Between(this.lastTimestamp, Instant.now()))

        val pages = conn?.vaultQueryByCriteria(
                contractStateType = LinearState::class.java,
                criteria = QueryCriteria.VaultQueryCriteria().withTimeCondition(timeCondition))

        logger.debug("{}: there are {} new events", this.x500Name, pages?.states?.size ?: 0)

        if (pages != null) {
            var states = pages.states.zip(pages.statesMetadata).sortedBy {  it.second.recordedTime }

            states.forEach { (state, meta) ->
                notifyEvent(state, meta.recordedTime)

                this.lastTimestamp = meta.recordedTime
            }
        }
    }

    private fun notifyEvent(sr: StateAndRef<ContractState>, recordedTime: Instant) {
        val state = sr.state.data as LinearState

        val stateType = when (state) {
            is FirstNotificationOfLossContract.State -> "FNOL"
            is PolicyContract.State -> "POLICY"
            is ClaimContract.State -> "CLAIM"
            else -> null
        }

        if (stateType != null) {
            val message = StateChangedEventImpl(
                    node = this.x500Name,
                    stateType = StateType(stateType),
                    stateLinearId = LinearId(id = UUID.randomUUID()),
                    stateId = sr.ref.toString(),
                    recordedTime = recordedTime
            )

            this.rabbitTemplate.convertAndSend("default", message)
        } else {
            logger.info("event {} is ignored as having a not-relevant type", state)
        }
    }
}
