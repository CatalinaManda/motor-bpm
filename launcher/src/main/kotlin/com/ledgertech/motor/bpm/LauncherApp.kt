package com.ledgertech.motor.bpm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages=["com.ledgertech.motor"])
@SpringBootApplication(exclude = [ArtemisAutoConfiguration::class, LiquibaseAutoConfiguration::class])
class LauncherApp {
}

fun main(args: Array<String>) {
    runApplication<LauncherApp>()
}
