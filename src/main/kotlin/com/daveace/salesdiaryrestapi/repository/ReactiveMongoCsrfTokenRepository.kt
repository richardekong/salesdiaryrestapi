package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.SalesDiaryCsrfToken
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface ReactiveMongoCsrfTokenRepository:
    ReactiveMongoRepository<SalesDiaryCsrfToken, String> {

        fun findSalesDiaryCsrfTokenBySessionId(id:String): Mono<SalesDiaryCsrfToken>
}

