package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.domain.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono


interface ReactiveUserRepository : ReactiveMongoRepository<User, String> {

    fun findUserByEmail(email: String): Mono<User>

    fun existsByEmail(email: String): Mono<Boolean> {
        return findUserByEmail(email).map { it != null }
    }

}