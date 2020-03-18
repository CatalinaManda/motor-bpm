package com.ledgertech.motor.bpm.config

import com.ledgertech.motor.bpm.MessageDispatcher
import com.ledgertech.motor.bpm.greeter.GreetReceiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

internal class BrokerMessageDispatcher: MessageDispatcher {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(BrokerMessageDispatcher::class.java)
    }

    @Autowired lateinit var greetReceiver: GreetReceiver

    override fun handleMessage(msg: String) {
        this.greetReceiver.receive(msg)
    }

    override fun handleMessage(msg: Any) {
        logger.warn("Unknown message received: {}", msg)
    }
}

@Configuration
class BrokerConfig {
    internal var queueName = "default"

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BrokerConfig::class.java)
    }

    @Bean
    internal fun brokerMessageDispatcher(): MessageDispatcher {
        return BrokerMessageDispatcher()
    }

    @Bean
    internal fun listenerAdapter(): MessageListenerAdapter {
        return MessageListenerAdapter(brokerMessageDispatcher())
    }

    @Bean
    internal fun container(connectionFactory: ConnectionFactory,
                           listenerAdapter: MessageListenerAdapter): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer()

        container.connectionFactory = connectionFactory
        container.setQueueNames(this.queueName)
        container.setMessageListener(listenerAdapter)

        return container
    }
}