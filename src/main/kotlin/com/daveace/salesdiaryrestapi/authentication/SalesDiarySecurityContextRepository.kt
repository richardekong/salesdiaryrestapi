package com.daveace.salesdiaryrestapi.authentication

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SalesDiarySecurityContextRepository : ServerSecurityContextRepository {

    private lateinit var authenticationManager: SalesDiaryReactiveAuthenticationManager


    companion object {
        private const val PREFIX = "Bearer\u0020"
    }

    @Autowired
    fun setAuthenticationManager(authManager: SalesDiaryReactiveAuthenticationManager) {
        this.authenticationManager = authManager
    }

    override fun save(p0: ServerWebExchange?, p1: SecurityContext?): Mono<Void> {
        return Mono.fromRunnable { SecurityContextHolder.setContext(p1) }
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        val request: ServerHttpRequest = exchange.request
        val authHeader: String? = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader != null && authHeader.startsWith(PREFIX)) {
            val token: String = authHeader.substring(PREFIX.length)
            return createReactiveSecurityContext(token).flatMap {
                save(exchange, it).apply { subscribe() }
                Mono.just(it)
            }
        }
    return Mono.empty()
}

private fun createReactiveSecurityContext(token: String): Mono<SecurityContext> {
    val authentication: Authentication = UsernamePasswordAuthenticationToken(token, token)
    return this.authenticationManager.authenticate(authentication).map {
        val securityContext = SecurityContextImpl(it)
        securityContext
    }
}

}

