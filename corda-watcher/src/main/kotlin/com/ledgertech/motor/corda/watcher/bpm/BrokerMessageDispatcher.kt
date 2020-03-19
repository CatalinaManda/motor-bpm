package com.ledgertech.motor.corda.watcher.bpm

import com.ledgertech.motor.corda.messages.ReportedClaimProcessEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class BrokerMessageDispatcher: MessageDispatcher {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(BrokerMessageDispatcher::class.java)
    }

    override fun handleMessage(msg: ReportedClaimProcessEvent) {
        logger.debug("Message received: {}", msg)
    }

    override fun handleMessage(msg: Any) {
        logger.warn("Unknown message received: {}", msg)
    }
}