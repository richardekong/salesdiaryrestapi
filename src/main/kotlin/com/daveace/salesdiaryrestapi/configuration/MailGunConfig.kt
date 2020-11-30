package com.daveace.salesdiaryrestapi.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Configuration
@PropertySource("classpath:mailgun.config.properties")
class MailGunConfig {

    @Value("\${mailgun.api.base.url}")
    private lateinit var mailGunBaseUri: String

    @Value("\${mailgun.api.domain.name}")
    private lateinit var mailGunDomainName: String

    @Value("\${mailgun.api.messages.url}")
    private lateinit var mailGunMessageUri: String

    @Value("\${mailgun.api.key}")
    private lateinit var mailGunApiKey: String

    @Value("\${mailgun.api.username}")
    private lateinit var mailGunUsername: String

    @Value("\${mailgun.api.authorization.prefix}")
    private lateinit var mailGunAuthorizationPrefix: String

    @Bean
    fun mailGunDomainName(): String = mailGunDomainName

    @Bean
    fun mailGunBaseUri(): String = mailGunBaseUri

    @Bean
    fun mailGunMessageUri(): String = mailGunMessageUri

    @Bean
    fun mailGunUrl(): String = mailGunBaseUri().plus(mailGunMessageUri())

    @Bean
    fun mailGunApiKey(): String = mailGunApiKey

    @Bean
    fun mailGunWebClient(): WebClient = WebClient.create(mailGunBaseUri)

    @Bean
    fun mailGunAuthorization(): String {
        val authenticationInBytes: ByteArray = "$mailGunUsername:$mailGunApiKey".toByteArray()
        val prefixAndSpace = "$mailGunAuthorizationPrefix\u0020"
        val base64Encoder: Base64.Encoder = Base64.getEncoder()
        return "$prefixAndSpace${base64Encoder.encodeToString(authenticationInBytes)}"
    }

}

