package com.ledgertech.motor.corda.watcher

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component(value = "queueGreeter")
class QueueGreeter (
        @Autowired val rabbitTemplate: RabbitTemplate
): CommandLineRunner {
    override fun run(vararg strings: String) {
        val messageText = "Corda watcher is running."

        this.rabbitTemplate.convertAndSend("default", messageText)
    }
}
