package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Mail
import org.apache.http.HttpHeaders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI

@Service
class MailServiceImpl : MailService {

    private lateinit var mailGunWebClient: WebClient
    private lateinit var mailGunDomainName: String
    private lateinit var mailGunBaseUri: String
    private lateinit var mailGunMessageUri: String
    private lateinit var mailGunApiKey: String
    private lateinit var mailGunAuthorization: String

    private companion object {
        const val FROM = "from"
        const val TO = "to"
        const val SUBJECT = "subject"
        const val TEXT = "text"
        const val HTML = "html"
        const val AUTHORIZATION = HttpHeaders.AUTHORIZATION
    }

    @Autowired
    fun setMailGunWebClient(mailGunWebClient: WebClient) {
        this.mailGunWebClient = mailGunWebClient
    }

    @Autowired
    fun setMailGunDomainName(mailGunDomainName: String) {
        this.mailGunDomainName = mailGunDomainName
    }

    @Autowired
    fun setMailGunMessageUri(mailGunMessageUri: String) {
        this.mailGunMessageUri = mailGunMessageUri
    }

    @Autowired
    fun setMailGunBaseUrl(mailGunBaseUri: String) {
        this.mailGunBaseUri = mailGunBaseUri
    }

    @Autowired
    fun setMailGunApiKey(mailGunApiKey: String) {
        this.mailGunApiKey = mailGunApiKey
    }

    @Autowired
    fun setMailGunAuthorization(mailGunAuthorization: String) {
        this.mailGunAuthorization = mailGunAuthorization
    }

    override fun sendText(mail: Mail): Mono<String> {
        return send(mail, contentType = TEXT)
    }

    override fun sendHTML(mail: Mail): Mono<String> {
        return send(mail, contentType = HTML)
    }

    private fun send(mail: Mail, contentType: String): Mono<String> {
        return mailGunWebClient
                .post()
                .uri { buildMailGunQueryParams(it, mail, contentType) }
                .header(AUTHORIZATION, mailGunAuthorization)
                .retrieve()
                .bodyToMono(String::class.java)
                .onErrorMap { RuntimeException("Failed to Send mail.") }
    }

    private fun buildMailGunQueryParams(uriBuilder: UriBuilder, mail: Mail, contentType: String): URI {
        return uriBuilder
                .path("$mailGunDomainName$mailGunMessageUri")
                .queryParam(FROM, mail.from)
                .queryParam(TO, mail.to)
                .queryParam(SUBJECT, mail.subject)
                .queryParam(contentType, mail.content)
                .build()
    }

}

