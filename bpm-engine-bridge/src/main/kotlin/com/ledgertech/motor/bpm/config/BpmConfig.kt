package com.ledgertech.motor.bpm.config

import com.ledgertech.motor.bpm.BrokerMessageDispatcher
import com.ledgertech.motor.bpm.MessageDispatcher
import com.ledgertech.motor.bpm.corda.CordaStateReceiver
import com.ledgertech.motor.bpm.greeter.GreetReceiver
import com.ledgertech.motor.corda.messages.StateChangedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("bpmConfiguration")
@ConfigurationProperties(prefix = "ledgertech.bpm")
class BpmConfig {
    class EventsConfig {
        var queueName: String = "default"
    }

    var events = EventsConfig()

    @Value("#{cordaConfiguration.events.queueName}")
    val cordaQueueName: String = "default"

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BpmConfig::class.java)
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
        container.setQueueNames(this.cordaQueueName)
        container.setMessageListener(listenerAdapter)

        return container
    }
}