package com.ledgertech.motor.bpm.services.amqp

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class Sender (@Autowired val rabbitTemplate: RabbitTemplate): CommandLineRunner {
    override fun run(vararg strings: String) {
        val messageText = "Hello World"

        this.rabbitTemplate.convertAndSend("default", messageText)
    }
}
