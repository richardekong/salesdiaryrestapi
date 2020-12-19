package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.Expenditure
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveExpenditureRepository:ReactiveMongoRepository<Expenditure, String> {
    override fun findById(id:String): Mono<Expenditure>
    override fun findAll(): Flux<Expenditure>
    fun findExpenditureByTraderId(id: String):Mono<Expenditure>
    fun deleteExpenditureById(id:String):Mono<Void>
}