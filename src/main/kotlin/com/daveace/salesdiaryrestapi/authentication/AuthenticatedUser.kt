package com.daveace.salesdiaryrestapi.authentication

import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.service.ReactiveUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticatedUser {

    @Autowired
    private lateinit var userService: ReactiveUserService

    fun ownsThisAccount(email: String): Mono<User> {
        return getCurrentUser()
                .filter { it.email == email }
                .switchIfEmpty(Mono.fromRunnable {
                    throw AuthenticationException(
                            HttpStatus.UNAUTHORIZED.reasonPhrase)
                })
    }

    fun isCurrentUserAuthorizedByEmail(email: String): Mono<Boolean> {
        return getCurrentUser()
                .filter { it.email == email }
                .switchIfEmpty(Mono.fromRunnable{
                    throw AuthenticationException(
                            HttpStatus.UNAUTHORIZED.reasonPhrase
                    )
                })
                .map { it != null }

    }

    fun isCurrentUserAuthorizedById(id: String): Mono<Boolean> {
        return getCurrentUser()
                .filter { it.id == id }
                .switchIfEmpty(Mono.fromRunnable{
                    throw AuthenticationException(
                            HttpStatus.UNAUTHORIZED.reasonPhrase
                    )
                })
                .map { it != null }
    }

    fun ownsThisAccountById(id: String): Mono<Boolean> {
        return getCurrentUser().filter { it.id == id }.map { it != null }
    }

    fun getCurrentUser(): Mono<User> = Mono.just(
            SecurityContextHolder
                    .getContext()
                    .authentication
                    .principal as String)
            .flatMap {
                userService.findUserByEmail(it)
            }

}