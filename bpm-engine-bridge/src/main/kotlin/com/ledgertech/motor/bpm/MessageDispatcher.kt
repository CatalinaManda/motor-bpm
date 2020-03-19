package com.ledgertech.motor.bpm

import com.ledgertech.motor.corda.messages.StateChangedEvent

interface MessageDispatcher {
    fun handleMessage(msg: String)
    fun handleMessage(msg: StateChangedEvent)
    fun handleMessage(msg: Any)
}