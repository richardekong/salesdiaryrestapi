package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Mail
import org.springframework.web.server.ServerWebExchange
import javax.validation.constraints.NotNull

interface MailTemplatingService{

    fun createMailFromTemplate(@NotNull exchange:ServerWebExchange): Mail
}