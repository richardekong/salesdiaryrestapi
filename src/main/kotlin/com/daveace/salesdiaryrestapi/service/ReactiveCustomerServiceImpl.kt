package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
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
    private lateinit var authenticatedUser: AuthenticatedUser

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
        return authenticatedUser.getCurrentUser()
                .flatMap { currentUser ->
                    customerRepo.findById(id)
                            .subscribeOn(Schedulers.parallel())
                            .filter { customer ->
                                customer.traderId == currentUser.id
                            }
                            .switchIfEmpty(Mono.fromRunnable {
                                throw RestException(HttpStatus.UNAUTHORIZED.reasonPhrase)
                            })
                }
    }

    override fun findCustomerByEmail(email: String): Mono<Customer> {

        return authenticatedUser.getCurrentUser()
                .flatMap { currentUser ->
                    customerRepo.findCustomerByEmail(email)
                            .subscribeOn(Schedulers.parallel())
                            .filter { customer -> customer.traderId == currentUser.id }
                            .switchIfEmpty(Mono.fromRunnable {
                                throw RestException(HttpStatus.UNAUTHORIZED.reasonPhrase)
                            })
                }
    }

    override fun findAllCustomers(): Flux<Customer> {
        return authenticatedUser.getCurrentUser()
                .flatMapMany { currentUser ->
                    customerRepo.findAll()
                            .subscribeOn(Schedulers.parallel())
                            .filter { customer ->
                                customer.traderId == currentUser.id
                            }
                            .switchIfEmpty(Mono.fromRunnable {
                                throw RestException(HttpStatus.UNAUTHORIZED.reasonPhrase)
                            })
                }
    }

    override fun existsByEmail(email: String): Mono<Boolean> {
        return customerRepo.existsCustomerByEmail(email)
    }
}