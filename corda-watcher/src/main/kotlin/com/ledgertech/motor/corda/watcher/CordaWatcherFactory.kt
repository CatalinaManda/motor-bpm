package com.ledgertech.motor.corda.watcher

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class CordaWatcherFactory {
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun create(name: String, config: CordaConfiguration) = CordaWatcher(name, config)
}