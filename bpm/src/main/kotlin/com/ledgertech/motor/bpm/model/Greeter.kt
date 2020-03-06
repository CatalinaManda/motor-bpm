package com.ledgertech.motor.bpm.model

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("greeter")
class Greeter {
    companion object {
        val L = LoggerFactory.getLogger(Greeter::class.java)
    }

    fun say(what: String) {
        L.info("Said: $what")
    }
}
