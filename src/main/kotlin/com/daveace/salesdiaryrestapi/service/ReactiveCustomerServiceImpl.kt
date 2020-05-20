package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.repository.ReactiveCustomerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class ReactiveCustomerServiceImpl() : ReactiveCustomerService {

    private lateinit var customerRepo: ReactiveCustomerRepository

    @Autowired
    constructor(customerRepo: ReactiveCustomerRepository):this(){
        this.customerRepo = customerRepo
    }

    override fun findCustomerByEmail(email: String): Mono<Customer> {
        return customerRepo.findById(email)
                .subscribeOn(Schedulers.parallel())
                .switchIfEmpty(Mono.fromRunnable {
                    throw RestException(HttpStatus.NOT_FOUND.reasonPhrase)
                })
    }

    override fun findAllCustomers(): Flux<Customer> {
        return customerRepo.findAll()
    }
}