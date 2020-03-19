package com.ledgertech.motor.corda.watcher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@EnableScheduling
@Component
abstract class CordaWatcherInitializer {
    @Autowired lateinit var config: CordaConfiguration

    companion object {
        val logger: Logger = LoggerFactory.getLogger(CordaWatcherInitializer::class.java)
    }

    @PostConstruct
    fun init() {
        logger.info("Corda watcher enabled: {}", config.watcher.enabled)

        if (config.watcher.enabled) {
            this.config.nodes.forEach {
                getWatcher(it.name, this.config)
            }
        }
    }

    @Lookup
    abstract fun getWatcher(x500Name: String, config: CordaConfiguration): CordaWatcher
}
