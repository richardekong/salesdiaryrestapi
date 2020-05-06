package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.Product
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ReactiveProductRepository:ReactiveMongoRepository<Product, String> {
}