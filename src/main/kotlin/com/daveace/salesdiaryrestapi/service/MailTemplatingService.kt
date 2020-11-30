package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.domain.User
import org.springframework.web.server.ServerWebExchange
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext
import javax.validation.constraints.NotNull

interface MailTemplatingService{

    fun createMailFromTemplate(@NotNull exchange:ServerWebExchange): Mail
    fun createMailFromTemplate(data: MutableMap<String,Any>, templateFileName:String, context: SpringWebFluxContext):Mail
}