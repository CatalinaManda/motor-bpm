package com.ledgertech.motor.bpm.config

import com.ledgertech.motor.bpm.services.amqp.Receiver
import org.apache.commons.io.IOUtils
import org.apache.qpid.server.SystemLauncher
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.UrlResource
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Paths


@Configuration
class QpidConfig {
    internal var queueName = "qn"
    internal var amqpPort = "5672"
    internal val configFileName = "qpid-config.json"

    internal var homeDir = "/tmp/qpid/home"
    internal var workingDir = "/tmp/qpid/work"

    internal var configFileResource = "classpath:/qpid/qpid-config.json"

    @Bean
    internal fun receiver(): Receiver {
        return Receiver()
    }

    @Bean
    internal fun listenerAdapter(receiver: Receiver): MessageListenerAdapter {
        return MessageListenerAdapter(receiver, "receiveMessage")
    }

    @Bean
    @Throws(Exception::class)
    internal fun broker(): SystemLauncher {
        val broker = SystemLauncher()
        broker.startup(brokerOptions())
        return broker
    }

    internal fun brokerOptions(): Map<String, Any>  {
        val brokerOptions = mutableMapOf<String, Any> ()
        val configPath = Paths.get(this.homeDir, configFileName).toString()

        File(this.homeDir).mkdirs()
        File(this.workingDir).mkdirs()

        copy(this.configFileResource, configPath)
        brokerOptions.put("type", "Memory");
        brokerOptions.put("qpid.work_dir", this.workingDir)
        brokerOptions.put("qpid.amqp_port", this.amqpPort)
        brokerOptions.put("qpid.home_dir", this.homeDir)
        brokerOptions.put("startupLoggedToSystemOut", true)
        brokerOptions.put("initialConfigurationLocation",  configPath)

        return brokerOptions
    }

    @Bean
    internal fun container(connectionFactory: ConnectionFactory,
                           listenerAdapter: MessageListenerAdapter): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer()
        container.connectionFactory = connectionFactory
        container.setQueueNames(queueName)
        container.setMessageListener(listenerAdapter)
        return container
    }

    private fun copy(resource: String, path: String) {
        File(path).writer(Charset.forName("UTF8")).use { w ->
            InputStreamReader(UrlResource(resource).inputStream, "UTF8").use { r ->
                IOUtils.copy(r, w)
            }
        }
    }
}