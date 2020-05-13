package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Trader
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveTraderService {
    fun create(trader:Trader): Mono<Trader>
    fun findTrader(email:String):Mono<Trader>
    fun findAllTraders(): Flux<Trader>
    fun updateTrader(email:String):Mono<Trader>
}