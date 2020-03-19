package com.ledgertech.motor.corda.watcher.bpm

import com.ledgertech.motor.corda.messages.ReportedClaimProcessEvent
import com.ledgertech.motor.corda.watcher.CordaRPC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class BpmReceiver {
    @Autowired
    lateinit var rpc: CordaRPC

    fun receive(msg: ReportedClaimProcessEvent) {
        // TODO:
    }
}
