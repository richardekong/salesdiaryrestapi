package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.Credit
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveCreditRepository: ReactiveMongoRepository<Credit, String>{
    override fun findById(id:String): Mono<Credit>
    override fun findAll():Flux<Credit>
    fun findByCustomerId(id:String):Flux<Credit>
    fun findByProductId(id: String): Flux<Credit>
}