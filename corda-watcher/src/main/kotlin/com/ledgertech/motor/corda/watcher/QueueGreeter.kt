package com.ledgertech.motor.corda.watcher

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component(value = "queueGreeter")
class QueueGreeter: CommandLineRunner {
    @Autowired lateinit var rabbitTemplate: RabbitTemplate
    @Autowired lateinit var config: CordaConfiguration

    override fun run(vararg strings: String) {
        val messageText = "Corda watcher is running."

        this.rabbitTemplate.convertAndSend(config.events.queueName, messageText)
    }
}
