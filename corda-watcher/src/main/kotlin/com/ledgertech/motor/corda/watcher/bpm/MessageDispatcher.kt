package com.ledgertech.motor.corda.watcher.bpm

import com.ledgertech.motor.corda.messages.ReportedClaimProcessEvent

interface MessageDispatcher {
    fun handleMessage(msg: ReportedClaimProcessEvent)
    fun handleMessage(msg: Any)
}