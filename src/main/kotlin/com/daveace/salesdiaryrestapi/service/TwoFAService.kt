package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.User
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

interface TwoFAService {

    fun activate2FA(user:User, channel: String, swe:ServerWebExchange): Mono<User>

    fun deActivate2FA(user:User, code:String):Mono<User>

    fun verify2FACode(email:String, code:String):Mono<User>

    fun request2FACode(user:User, swe:ServerWebExchange)

    fun update2FAChannel(user:User, channel: String, code: String):Mono<User>

    fun generateRandomCode(): String = (Random().nextInt(9999) + 10000).toString()
}

