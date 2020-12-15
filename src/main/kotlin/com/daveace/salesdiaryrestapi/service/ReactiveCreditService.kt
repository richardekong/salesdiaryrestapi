package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Credit
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveCreditService {

    fun createCreditRecord(credit:Credit):Mono<Credit>
    fun createCreditRecord(event: SalesEvent):Mono<Credit>
    fun findCreditById(id: String): Mono<Credit>
    fun findCreditsByCustomerId(id: String): Flux<Credit>
    fun findCreditsByProductId(id: String): Flux<Credit>
    fun findAllCredits(): Flux<Credit>
    fun redeemCredit(credit:Credit):Mono<Credit>
}