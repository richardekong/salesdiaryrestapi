package com.daveace.salesdiaryrestapi.authentication

import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import io.jsonwebtoken.Claims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class SalesDiaryReactiveAuthenticationManager : ReactiveAuthenticationManager {
    private lateinit var tokenUtil: TokenUtil

    @Autowired
    fun setTokenUtil(tokenUtil: TokenUtil) {
        this.tokenUtil = tokenUtil
    }

    override fun authenticate(auth: Authentication): Mono<Authentication> {
        val token: String = auth.credentials.toString()
        val email: String?
        email = try {
            tokenUtil.getEmailFromToken(token)
        } catch (e: Exception) {
            throw AuthenticationException("invalid token")
        }
        if (tokenUtil.isTokenExpired(token))
            throw AuthenticationException("Token has expired")
        val claims: Claims = tokenUtil.getAllClaimsFromToken(token)
        val roles: List<*>? = claims.get(User.ROLE, List::class.java)
        val authorities: List<SimpleGrantedAuthority>? = roles
                ?.asSequence()
                ?.map { role -> SimpleGrantedAuthority(role as String) }
                ?.toList()
        val uNamePwdToken = UsernamePasswordAuthenticationToken(email, null, authorities)
        return Mono.just(uNamePwdToken)
    }

}