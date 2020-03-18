package com.ledgertech.motor.bpm.greeter

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("greeter")
class Greeter {
    companion object {
        val L = LoggerFactory.getLogger(Greeter::class.java)
    }

    fun hello(what: String) {
        L.info("The process said: $what")
    }
}
