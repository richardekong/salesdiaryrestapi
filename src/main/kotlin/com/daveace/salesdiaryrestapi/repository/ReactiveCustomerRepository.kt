package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.Customer
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ReactiveCustomerRepository: ReactiveMongoRepository<Customer, String> {
}