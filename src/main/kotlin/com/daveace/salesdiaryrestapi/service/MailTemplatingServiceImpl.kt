package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.controller.BaseController.Companion.MAIL
import com.daveace.salesdiaryrestapi.controller.BaseController.Companion.RECIPIENT_DATA
import com.daveace.salesdiaryrestapi.controller.BaseController.Companion.TEMPLATE_FILE_NAME
import com.daveace.salesdiaryrestapi.domain.Mail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import org.thymeleaf.spring5.SpringWebFluxTemplateEngine
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext
import javax.validation.constraints.NotNull

@Service
class MailTemplatingServiceImpl:MailTemplatingService {

    private lateinit var templateEngine: SpringWebFluxTemplateEngine

    @Value("\${mailgun.api.email}")
    private lateinit var appEmail: String


    @Autowired
    fun initTemplateEngine(templateEngine: SpringWebFluxTemplateEngine) {
        this.templateEngine = templateEngine
    }

    override fun createMailFromTemplate(@NotNull exchange: ServerWebExchange):Mail {
        val context = SpringWebFluxContext(exchange, exchange.localeContext.locale)
        context.setVariable(RECIPIENT_DATA, exchange.getAttribute(RECIPIENT_DATA))
        val content: String = templateEngine.process((exchange.getAttribute<String?>(TEMPLATE_FILE_NAME)), context)
        return exchange.getAttribute<Mail>(MAIL)!!.apply {
            this.from = appEmail
            this.content = content
        }
    }

}