package com.ledgertech.motor.corda.watcher

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@EnableScheduling
@Component
abstract class CordaWatcherLauncher {
    @Autowired lateinit var config: CordaConfiguration

    @PostConstruct
    fun init() {
        this.config.nodes.forEach {
            getWatcher(it.name)
        }
    }

    @Lookup
    abstract fun getWatcher(x500Name: String): CordaWatcher
}
