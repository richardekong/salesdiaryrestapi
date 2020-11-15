package com.daveace.salesdiaryrestapi.configuration

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.TWO_FACTOR_AUTH
import com.daveace.salesdiaryrestapi.domain.TwoFAData
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import com.daveace.salesdiaryrestapi.service.SalesDiaryPasswordEncoderService
import com.daveace.salesdiaryrestapi.service.TwoFAMessageService
import com.daveace.salesdiaryrestapi.service.TwoFAService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Configuration
class TwoFAFilter : WebFilter {

    private lateinit var twoFAService: TwoFAService
    private lateinit var twoFAMessageService: TwoFAMessageService
    private lateinit var userRepo: ReactiveUserRepository
    private lateinit var encoderService: SalesDiaryPasswordEncoderService
    private val httpMethodsToIntercept = arrayOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE)
    private val excludedHttpRequests = arrayOf("$API$SALES_DIARY_USERS$TWO_FACTOR_AUTH")

    companion object {
        const val CODE = "code"
    }

    @Autowired
    private fun initTwoFAService(twoFAService: TwoFAService) {
        this.twoFAService = twoFAService
    }

    @Autowired
    private fun initTwoFAMessageService(twoFAMessageService: TwoFAMessageService) {
        this.twoFAMessageService = twoFAMessageService
    }

    @Autowired
    private fun initUserRepo(userRepo: ReactiveUserRepository) {
        this.userRepo = userRepo
    }

    @Autowired
    private fun initEncoderService(encoderService: SalesDiaryPasswordEncoderService) {
        this.encoderService = encoderService
    }

    override fun filter(exchange: ServerWebExchange, filter: WebFilterChain): Mono<Void> {
        return interceptRequest(exchange, filter)
    }

    private fun interceptRequest(exchange: ServerWebExchange, filter: WebFilterChain): Mono<Void> {
        val method = exchange.request.method
        val path = exchange.request.path.toString()
        val twoFACode = exchange.request.headers.getFirst(CODE)
        if (SecurityContextHolder.getContext().authentication != null &&
                method in httpMethodsToIntercept &&
                path !in excludedHttpRequests) {
            val email = SecurityContextHolder.getContext().authentication.principal.toString()
            userRepo.findUserByEmail(email)
                    .filter { it != null && it.twoFAData.enabled }
                    .map {
                        if (
                                twoFACode.isNullOrBlank() ||
                                it.twoFAData.isExpired() ||
                                !encoderService.matches(twoFACode, it.twoFAData.code)) {
                            updateUserThenSendValid2FAData(it, exchange).subscribe()
                        }
                    }.apply { subscribe() }
        }
        return filter.filter(exchange)
    }

    private fun updateUserThenSendValid2FAData(it: User, exchange: ServerWebExchange): Mono<User> {
        val randomCode = twoFAService.generateRandomCode()
        return userRepo.save(it.apply {
            update2FAData(randomCode)
        }).doOnSuccess { updatedUser ->
            twoFAMessageService.send(updatedUser, randomCode, exchange)
            requestValid2FAData()
        }
    }

    private fun requestValid2FAData() {
        throw AuthenticationException("Please check your inbox for a new 2FA code," +
                " and provide a valid 2FA code to perform this action")
    }

    private fun User.update2FAData(randomCode: String) {
        twoFAData.code = encoderService.encode(randomCode)
        twoFAData.validity = LocalDateTime.now().plusMinutes(TwoFAData.TimeFrame.MEDIUM.timeFrame)
    }
}

