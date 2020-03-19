package com.ledgertech.motor.corda.messages

class ProcessEventType {
    companion object {
        val REPORT_CLAIM_PROCESS_STARTED = StateType("REPORT_CLAIM_PROCESS_STARTED")
    }
}

interface ReportedClaimProcessEvent: Event {
    val stateLinearId: LinearId
}

data class ReportedClaimProcessEventImpl constructor(
        override val node: String,
        override val stateLinearId: LinearId
): ReportedClaimProcessEvent {
    override var payload: Any? = null
        private set

    override val eventType: String = ProcessEventType.REPORT_CLAIM_PROCESS_STARTED.name
}
