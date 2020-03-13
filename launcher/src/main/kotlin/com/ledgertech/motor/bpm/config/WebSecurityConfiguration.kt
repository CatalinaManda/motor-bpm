package com.ledgertech.motor.bpm.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration: WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        // @formatter:off
        http
            ?.csrf()?.disable()
            ?.cors()
            ?.and()
            ?.authorizeRequests()
                ?.antMatchers("/test**")?.permitAll()
                ?.antMatchers("/api/**")?.permitAll()
                ?.antMatchers("/rest/**")?.permitAll()
                //?.and()
            //?.oauth2ResourceServer()
              //  ?.jwt()

        // @formatter:on
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("*")
            allowedHeaders = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
