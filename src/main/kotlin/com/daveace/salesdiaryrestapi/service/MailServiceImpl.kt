package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Mail
import org.apache.http.HttpHeaders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI
import java.net.URL

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
        return send(mail, contentType = TEXT).apply { subscribe() }
    }

    override fun sendHTML(mail: Mail): Mono<String> {
        return send(mail, contentType = HTML).apply { subscribe() }
    }

    private fun send(mail: Mail, contentType: String): Mono<String> {
        return isMailGunReachable()
                .filter { reachable -> reachable }
                .flatMap {
                    mailGunWebClient.post()
                            .uri { buildMailGunQueryParams(it, mail, contentType) }
                            .header(AUTHORIZATION, mailGunAuthorization)
                            .retrieve()
                            .bodyToMono(String::class.java)
                }
                .switchIfEmpty(Mono.just("Mail server is unreachable"))
                .onErrorMap {
                    RuntimeException("Failed to Send mail.")
                }

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

    private fun isMailGunReachable(): Mono<Boolean> {
        return run {
            try {
                URL(mailGunBaseUri)
                        .openConnection()
                        .connect()
            } catch (exp: Exception) {
                return Mono.just(false)
            }
            Mono.just(true)
        }
    }

}

