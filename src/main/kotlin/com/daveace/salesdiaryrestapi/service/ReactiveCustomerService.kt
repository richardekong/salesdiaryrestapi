package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Customer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveCustomerService {
    fun saveIfAbsent(customer:Customer):Mono<Customer>
    fun findCustomerById(id:String):Mono<Customer>
    fun findCustomerByEmail(email:String): Mono<Customer>
    fun findAllCustomers():Flux<Customer>
    fun existsByEmail(email: String):Mono<Boolean>
}

