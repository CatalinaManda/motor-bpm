package com.ledgertech.motor.bpm.config

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig
import org.springframework.stereotype.Component
import javax.ws.rs.ApplicationPath

@Component
@ApplicationPath("/api/bpm")
class JerseyConfig: CamundaJerseyResourceConfig() {
    override fun registerAdditionalResources() {
    }
}