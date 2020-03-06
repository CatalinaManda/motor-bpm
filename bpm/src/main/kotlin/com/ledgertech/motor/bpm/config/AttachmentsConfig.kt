package com.ledgertech.motor.bpm.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "ledgertech.bpm")
class AttachmentsConfig() {
}