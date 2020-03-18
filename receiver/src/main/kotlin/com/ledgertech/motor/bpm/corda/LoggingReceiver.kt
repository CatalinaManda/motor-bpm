package com.ledgertech.motor.bpm.corda

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LoggingReceiver {
    companion object {
        val L: Logger = LoggerFactory.getLogger(LoggingReceiver::class.java)
    }

    fun receive(data: String) {
        L.info("Logging receiver: {}", data)
    }
}