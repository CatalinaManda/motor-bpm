package com.ledgertech.motor.corda.messages

data class EventType(val name: String)

interface Event {
    val eventType: String

    /**
     * X500 name of the node owner
     */
    val node: String

    val payload: Any?
}
