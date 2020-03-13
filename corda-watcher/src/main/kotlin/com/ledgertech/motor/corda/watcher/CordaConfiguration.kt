package com.ledgertech.motor.corda.watcher

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "ledgertech.corda")
class CordaConfiguration {
    var nodes: List<CordaNodeConfiguration> = mutableListOf()
}

class CordaNodeConfiguration {
    lateinit var name: String
    lateinit var host: String
    lateinit var port: String
    lateinit var user: String
    lateinit var password: String

    override fun toString(): String = "name: $name, host: $host:$port"
}