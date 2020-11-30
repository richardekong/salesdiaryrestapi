package com.daveace.salesdiaryrestapi.configuration

import com.daveace.salesdiaryrestapi.domain.GmailCredentials
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:gmail.oauth.config.properties")
class GmailConfig {

    @Value("\${gmail.user.email}")
    private lateinit var userEmail: String
    @Value("\${gmail.client-id}")
    private lateinit var clientId: String
    @Value("\${gmail.client-secret}")
    private lateinit var clientSecret: String
    @Value("\${gmail.access_token}")
    private lateinit var accessToken: String
    @Value("\${gmail.refresh_token}")
    private lateinit var refreshToken: String

    @Bean
    fun transport():HttpTransport{
        return GoogleNetHttpTransport.newTrustedTransport()
    }

    @Bean
    fun gmailCredentials(): GmailCredentials {
        return GmailCredentials(
                userEmail,
                clientId,
                clientSecret,
                accessToken,
                refreshToken
        )
    }

    @Bean
    fun googleCredential(): GoogleCredential {
        return gmailCredentials().run {
            GoogleCredential.Builder().setTransport(transport())
                    .setJsonFactory(JacksonFactory.getDefaultInstance()!!)
                    .setClientSecrets(clientId, clientSecret)
                    .build()
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken)
        }
    }

}