package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Product
import reactor.core.publisher.Mono


interface ReactiveProductService {

    fun saveIfAbsent(product: Product):Mono<Product>
    fun findProduct(id:String): Mono<Product>
    fun existsByName(name:String):Mono<Boolean>
}