package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

interface ReactiveCustomerRepository : ReactiveMongoRepository<Customer, String> {
    fun findCustomerByEmail(email: String): Mono<Customer>

    fun existsCustomerByEmail(email: String):Mono<Boolean>{
        return findCustomerByEmail(email).map { it != null }
    }
}