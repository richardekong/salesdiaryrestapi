package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.User
import org.springframework.web.server.ServerWebExchange

interface TwoFAMessageService {

    fun send(user: User, text:String, swe:ServerWebExchange)
}

