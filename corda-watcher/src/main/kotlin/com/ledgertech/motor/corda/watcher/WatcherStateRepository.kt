package com.ledgertech.motor.corda.watcher

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class WatcherStateRepository {
    var time: Instant = Instant.now()

    fun getTime(x500Name: String): Instant {
        return this.time
    }

    fun updateTime(x500Name: String, time: Instant) {
        this.time = time
    }
}