package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.User
import org.springframework.hateoas.Link
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveUserService : ReactiveUserDetailsService {
    fun create(user:User):Mono<User>
    fun save(user: User): Mono<User>
    override fun findByUsername(email: String): Mono<UserDetails>
    fun findUserByEmail(email: String): Mono<User>
    fun findAll(): Flux<User>
    fun sendPasswordResetLink(email: String, monoLink:Mono<Link>):Mono<String>
    fun resetUserPassword(token: String, newPassword:String):Mono<User>
    fun deleteUserByEmail(email:String):Mono<Void>
}