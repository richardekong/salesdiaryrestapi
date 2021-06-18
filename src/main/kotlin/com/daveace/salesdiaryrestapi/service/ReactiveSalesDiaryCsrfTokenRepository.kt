package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.SalesDiaryCsrfToken
import com.daveace.salesdiaryrestapi.repository.ReactiveMongoCsrfTokenRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.security.web.server.csrf.DefaultCsrfToken
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

@Service
class ReactiveSalesDiaryCsrfTokenRepository : ServerCsrfTokenRepository {

    private lateinit var csrfTokenRepository: ReactiveMongoCsrfTokenRepository
    private val tag: String = "sales-diary"

    companion object {
        const val CSRF_HEADER_NAME = "X-CSRF-TOKEN"
        const val CSRF_PARAMETER_NAME = "_csrf"
    }


    @Autowired
    fun initCsrfTokenRepository(csrfTokenRepository: ReactiveMongoCsrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository
    }

    override fun generateToken(ex: ServerWebExchange?): Mono<CsrfToken> {
        return Mono.just(
            DefaultCsrfToken(
                CSRF_HEADER_NAME,
                CSRF_PARAMETER_NAME,
                "$tag-${UUID.randomUUID()}"
            )
        )
    }

    override fun saveToken(ex: ServerWebExchange?, csrfToken: CsrfToken?): Mono<Void> {
        val sessionId:String = obtainSessionId(ex)

       return  csrfTokenRepository.findSalesDiaryCsrfTokenBySessionId(sessionId)
            .filter { it != null }
            .switchIfEmpty(Mono.fromRunnable {
                //if csrf token does not exists
                val token = SalesDiaryCsrfToken()
                token.token = csrfToken?.token!!
                token.sessionId = sessionId
            })
            .map {
                //if csrf token exists
                val token:SalesDiaryCsrfToken = it
                token.token = csrfToken?.token!!
            }.then()
    }

    override fun loadToken(ex: ServerWebExchange?): Mono<CsrfToken> {
        val sessionId = obtainSessionId(ex)
        return csrfTokenRepository.findSalesDiaryCsrfTokenBySessionId(sessionId)
            .filter { it!=null }
            .map{
                DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_PARAMETER_NAME, it.token)
            }
    }

    private fun obtainSessionId(ex: ServerWebExchange?) = ex!!.session.filter { it != null }
        .switchIfEmpty(Mono.fromRunnable {
            throw RuntimeException("No session!")
        }).toFuture().join().id
}