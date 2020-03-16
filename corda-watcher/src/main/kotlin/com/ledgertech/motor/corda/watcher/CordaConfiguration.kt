package com.ledgertech.motor.corda.watcher

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@Configuration
@ConfigurationProperties(prefix = "ledgertech.corda")
class CordaConfiguration {
    var nodes: List<CordaNodeConfiguration> = mutableListOf()
    var watcher: WatcherConfig = WatcherConfig()
}

class CordaNodeConfiguration {
    lateinit var name: String
    lateinit var host: String
    lateinit var port: String
    lateinit var user: String
    lateinit var password: String

    override fun toString(): String = "name: $name, host: $host:$port"
}

class WatcherConfig {
    var fixedRate: Long = 5000
    var initialDelay: Long = 1000
    var queueName: String = "default"
}