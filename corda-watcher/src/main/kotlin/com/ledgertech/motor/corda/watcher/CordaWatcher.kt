package com.ledgertech.motor.corda.watcher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.Scheduled

const val le: String = "ledgertech.corda.watcher.fixedRate"

@Scope("prototype")
class CordaWatcher(val x500Name: String) {
    @Autowired lateinit var rpc: CordaRPC
    @Autowired lateinit var config: CordaConfiguration

    init {
        logger.info("CREATED")
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(CordaWatcher::class.java)
    }

    @Scheduled(fixedRateString ="#{cordaConfiguration.watcher.fixedRate}", initialDelay=1000)
    fun searchForUpdates() {
        logger.info("HELLO from {}", this.x500Name)
    }
}