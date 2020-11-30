package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.Trader
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface ReactiveTraderRepository: ReactiveMongoRepository<Trader, String> {

    fun findTraderByEmail(email:String): Mono<Trader>

    fun existsTraderByEmail(email: String):Mono<Boolean>{
        return findTraderByEmail(email).map{ it != null}
    }
}