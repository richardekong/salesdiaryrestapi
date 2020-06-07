package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.Product
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

interface ReactiveProductRepository:ReactiveMongoRepository<Product, String> {

    fun findProductByName(name:String): Mono<Product>

    fun existsByName(name: String):Mono<Boolean>{
        return findProductByName(name)
                .subscribeOn(Schedulers.parallel())
                .map { it != null }
    }
}