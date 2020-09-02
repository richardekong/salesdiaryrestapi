package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.configuration.SortConfigurationProperties
import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.hateoas.link.ReactiveLinkSupport
import com.daveace.salesdiaryrestapi.page.Paginator
import com.daveace.salesdiaryrestapi.service.MailService
import com.daveace.salesdiaryrestapi.service.MailTemplatingService
import com.sun.istack.internal.logging.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class BaseController : ReactiveLinkSupport {

    lateinit var paginator: Paginator
    lateinit var sortProps: SortConfigurationProperties
    lateinit var tokenUtil: TokenUtil
    lateinit var authenticatedUser: AuthenticatedUser
    lateinit var mailService: MailService
    lateinit var mailTemplatingService: MailTemplatingService

    companion object {
        const val DEFAULT_SIZE = "1"
        const val DEFAULT_PAGE = "0"
        const val DEFAULT_SORT_FIELD = "id"
        const val DEFAULT_SORT_ORDER = "asc"
        const val TEMPLATE_FILE_NAME = "template_file_name"
        const val RECIPIENT_DATA = "data"
        const val MAIL = "mail"
        const val DATE_TIME_PATTERN = "MM-dd-yyyy hh:mm a"
        val LOGGER = Logger.getLogger(this::class.java)!!
    }

    @Autowired
    fun initPaginator(paginator: Paginator) {
        this.paginator = paginator
    }

    @Autowired
    fun initSortConfigurationProperties(sortProps: SortConfigurationProperties) {
        this.sortProps = sortProps
    }

    @Autowired
    fun initTokenUtil(tokenUtil: TokenUtil) {
        this.tokenUtil = tokenUtil
    }

    @Autowired
    fun initAuthenticatedUser(authenticatedUser: AuthenticatedUser) {
        this.authenticatedUser = authenticatedUser
    }

    @Autowired
    fun initMailService(mailService: MailService) {
        this.mailService = mailService
    }

    @Autowired
    fun initMailTemplatingService(mailTemplatingService: MailTemplatingService) {
        this.mailTemplatingService = mailTemplatingService
    }

    protected fun configureSortProperties(by: String, dir: String): SortConfigurationProperties {
        sortProps.by = by
        sortProps.dir = dir
        return sortProps
    }

    protected fun <T> throwAuthenticationException(): Mono<T> = Mono.fromRunnable { throw AuthenticationException(HttpStatus.UNAUTHORIZED.reasonPhrase) }

    protected fun prepareAndSendEmailWithHTMLTemplate(
            templateFileName: String = "", mail: Mail,
            recipientData: MutableMap<String, Any?>,
            exchange: ServerWebExchange): Mono<String> {

        exchange.attributes[TEMPLATE_FILE_NAME] = templateFileName
        exchange.attributes[RECIPIENT_DATA] = recipientData
        exchange.attributes[MAIL] = mail
        return mailService.run {
            val modifiedMail: Mail = mailTemplatingService
                    .createMailFromTemplate(exchange)
            println("\ncreated mail: $modifiedMail\n")
            sendHTML(modifiedMail).apply { subscribe({
                LOGGER.info("\nHTML Email Delivery:$it\n")
            },{
                LOGGER.severe("\nHTML Email Delivery:${it.message}\n")
            }) }
        }
    }
}