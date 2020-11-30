package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Product
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


interface ReactiveProductService {

    fun save(product: Product):Mono<Product>
    fun saveIfAbsent(product: Product):Mono<Product>
    fun findProduct(id:String): Mono<Product>
    fun findProducts(): Flux<Product>
    fun existsByName(name:String):Mono<Boolean>
}