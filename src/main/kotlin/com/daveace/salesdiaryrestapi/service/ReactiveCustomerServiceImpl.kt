package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.repository.ReactiveCustomerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class ReactiveCustomerServiceImpl() : ReactiveCustomerService {

    private lateinit var customerRepo: ReactiveCustomerRepository

    @Autowired
    constructor(customerRepo: ReactiveCustomerRepository) : this() {
        this.customerRepo = customerRepo
    }

    override fun saveIfAbsent(customer: Customer): Mono<Customer> {
        val email: String = customer.email
        return customerRepo.existsCustomerByEmail(email)
                .subscribeOn(Schedulers.parallel())
                .filter { customerExists ->
                    if (customerExists) throw RuntimeException(
                            "Customer with $email exists!")
                    else customerExists.not()
                }
                .flatMap { customerRepo.save(customer) }
    }

    override fun findCustomerById(id: String): Mono<Customer> {
        return customerRepo.findById(id).subscribeOn(Schedulers.parallel())
    }

    override fun findCustomerByEmail(email: String): Mono<Customer> {
        return customerRepo.findCustomerByEmail(email).subscribeOn(Schedulers.parallel())
    }

    override fun findAllCustomers(): Flux<Customer> {
        return customerRepo.findAll().subscribeOn(Schedulers.parallel())

    }

    override fun existsByEmail(email: String): Mono<Boolean> {
        return customerRepo.existsCustomerByEmail(email)
    }
}