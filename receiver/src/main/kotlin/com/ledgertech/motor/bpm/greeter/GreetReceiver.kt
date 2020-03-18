package com.ledgertech.motor.bpm.greeter

import org.camunda.bpm.engine.ProcessEngine
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GreetReceiver {
    companion object {
        val L = LoggerFactory.getLogger(GreetReceiver::class.java)
    }

    @Autowired
    lateinit var camunda: ProcessEngine

    fun receive(data: String) {
        L.info("Greet message: {}", data)
        L.info("Launch greeter BP")

        this.camunda.runtimeService.startProcessInstanceByKey("processGreet")
    }
}
