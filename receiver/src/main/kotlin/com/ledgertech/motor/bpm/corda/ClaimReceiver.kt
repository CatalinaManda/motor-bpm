package com.ledgertech.motor.bpm.corda

import org.camunda.bpm.engine.ProcessEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ClaimReceiver {
    companion object {
        val L: Logger = LoggerFactory.getLogger(ClaimReceiver::class.java)
    }

    @Autowired
    lateinit var camunda: ProcessEngine

    fun receive(data: String) {
        L.info("Claim receiver: {}", data)
    }
}