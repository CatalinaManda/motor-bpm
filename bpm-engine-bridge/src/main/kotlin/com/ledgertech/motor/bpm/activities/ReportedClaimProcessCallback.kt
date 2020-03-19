package com.ledgertech.motor.bpm.activities

import com.ledgertech.motor.bpm.config.BpmConfig
import com.ledgertech.motor.corda.messages.LinearId
import com.ledgertech.motor.corda.messages.ReportedClaimProcessEventImpl
import com.ledgertech.motor.corda.processes.ProcessReportedClaimMeta
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component("reportedClaimProcessCallback")
class ReportedClaimProcessCallback {
    @Autowired
    lateinit var config: BpmConfig

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    fun launchEvaluation(exec: ActivityExecution) {
        var linearId = exec.processInstance.getVariable(ProcessReportedClaimMeta.Variables.CLAIM_LINEAR_ID)!! as UUID
        var node = exec.processInstance.getVariable(ProcessReportedClaimMeta.Variables.NODE)!! as String

        this.rabbitTemplate.convertAndSend(config.events.queueName, ReportedClaimProcessEventImpl(node, LinearId(null, linearId)))
    }
}