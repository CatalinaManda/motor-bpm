package com.ledgertech.motor.bpm.services.amqp

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class Receiver {
    companion object {
        val L: Logger = LoggerFactory.getLogger(Receiver::class.java)
    }

    @RabbitListener(queues = ["default"])
    fun receive(data: String) {
        L.info("Message: {}", data)
    }
}