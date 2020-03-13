package com.ledgertech.motor.corda.messages

import java.time.Instant
import java.util.*

data class StateType(
        // Unique name of the type (e.g. Claim, Policy, etc)
        val name: String
)

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