package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Customer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveCustomerService {
    fun findCustomerByEmail(email:String): Mono<Customer>
    fun findAllCustomers():Flux<Customer>
}

