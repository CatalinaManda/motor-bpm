package com.ledgertech.motor.corda.messages

import java.time.Instant
import java.util.*

data class StateType(
        // Unique name of the type (e.g. Claim, Policy, etc)
        val name: String
) {
    companion object {
        val FNOL = StateType("FNOL")
        val POLICY = StateType("POLICY")
        val CLAIM = StateType("CLAIM")
    }
}

class StateEventType {
    companion object {
        val UPDATED = StateType("STATE_UPDATE")
        val CREATED = StateType("STATE_CREATED")
    }
}

/**
 *  One to one representation of corda unique id (used to represent linear id)
 */
data class LinearId(val externalId: String? = null, val id: UUID)

interface StateChangedEvent: Event {
    val stateType: StateType
    val stateLinearId: LinearId

    // Unique id of the new instance of state
    val stateId: String
    val recordedTime: Instant
}

data class StateUpdatedEventImpl private constructor(
        override val node: String,
        override val stateType: StateType,
        override val stateLinearId: LinearId,
        override val stateId: String,
        override val recordedTime: Instant
): StateChangedEvent {
    override var payload: Any? = null
        private set

    override val eventType: String = StateEventType.UPDATED.name

    public constructor(node: String,
                stateType: StateType,
                stateLinearId: LinearId,
                stateId: String,
                recordedTime: Instant,
                jsonClaim: String?): this(node, stateType, stateLinearId, stateId, recordedTime) {
        this.payload = jsonClaim
    }
}


data class StateCreatedEventImpl private constructor(
        override val node: String,
        override val stateType: StateType,
        override val stateLinearId: LinearId,
        override val stateId: String,
        override val recordedTime: Instant
): StateChangedEvent {
    override var payload: Any? = null
        private set

    override val eventType: String = StateEventType.CREATED.name

    public constructor(node: String,
                       stateType: StateType,
                       stateLinearId: LinearId,
                       stateId: String,
                       recordedTime: Instant,
                       jsonClaim: String?): this(node, stateType, stateLinearId, stateId, recordedTime) {
        this.payload = jsonClaim
    }
}

enum class Status {
    REPORTED,
    AUTO_EVALUATING,
    MANUAL_EVALUATING,
    EVALUATED
}

data class Claim(val status: Status) {
}