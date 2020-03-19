package com.ledgertech.motor.bpm.corda

import com.ledgertech.motor.corda.messages.Claim
import com.ledgertech.motor.corda.messages.StateChangedEvent
import com.ledgertech.motor.corda.messages.StateType
import com.ledgertech.motor.corda.messages.Status
import com.ledgertech.motor.corda.processes.ProcessReportedClaimMeta
import org.camunda.bpm.engine.ProcessEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CordaStateReceiver {
    companion object {
        val L: Logger = LoggerFactory.getLogger(CordaStateReceiver::class.java)
    }

    @Autowired
    lateinit var camunda: ProcessEngine

    fun receive(event: StateChangedEvent) {
        L.info("Claim received: {}", event)
        dispatchEvent(event)
    }

    private inline fun dispatchEvent(event: StateChangedEvent) {
        when (event.stateType) {
            StateType.CLAIM -> handleClaim(event)
            else -> {
                L.debug("Unknown state type {}, ignore.", event.stateType)
            }
        }
    }

    private inline fun handleClaim(event: StateChangedEvent) {
        var claim = event.payload as? Claim?

        if (claim == null) {
            L.debug("Unknown corda status, ignore.")
        } else {
            if (claim.status == Status.REPORTED) {
                L.info("New claim reported event.")
                handleReportedClaim(event, claim)
            }
        }
    }

    private inline fun handleReportedClaim(event: StateChangedEvent, claim: Claim) {
        this.camunda.runtimeService.startProcessInstanceByKey("processReportedClaim",
                mutableMapOf<String, Any>(
                        ProcessReportedClaimMeta.Variables.CLAIM_LINEAR_ID to event.stateLinearId.id,
                        ProcessReportedClaimMeta.Variables.NODE to event.node))
    }
}