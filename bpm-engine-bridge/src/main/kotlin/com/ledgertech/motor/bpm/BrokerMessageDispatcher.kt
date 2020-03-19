package com.ledgertech.motor.bpm

import com.ledgertech.motor.bpm.corda.CordaStateReceiver
import com.ledgertech.motor.bpm.greeter.GreetReceiver
import com.ledgertech.motor.corda.messages.StateChangedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

internal class BrokerMessageDispatcher: MessageDispatcher {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(BrokerMessageDispatcher::class.java)
    }

    @Autowired
    lateinit var greetReceiver: GreetReceiver
    @Autowired
    lateinit var cordaReceiver: CordaStateReceiver

    override fun handleMessage(msg: String) {
        logger.debug("Message received: {}", msg)
        this.greetReceiver.receive(msg)
    }

    override fun handleMessage(msg: StateChangedEvent) {
        logger.debug("Message received: {}", msg)
        this.cordaReceiver.receive(msg)
    }

    override fun handleMessage(msg: Any) {
        logger.warn("Unknown message received: {}", msg)
    }
}