package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository


interface ReactiveUserRepository: ReactiveMongoRepository<User, String> {
}