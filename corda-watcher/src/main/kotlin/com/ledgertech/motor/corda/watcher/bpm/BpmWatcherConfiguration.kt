package com.ledgertech.motor.corda.watcher.bpm

import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BpmWatcherConfiguration {
    @Value("#{bpmConfiguration.events.queueName}")
    val bpmQueueName: String = "default"

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
        container.setQueueNames(this.bpmQueueName)
        container.setMessageListener(listenerAdapter)

        return container
    }
}