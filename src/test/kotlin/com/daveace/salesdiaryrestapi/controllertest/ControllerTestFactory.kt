package com.daveace.salesdiaryrestapi.controllertest

import com.daveace.salesdiaryrestapi.controller.ControllerPath
import com.daveace.salesdiaryrestapi.domain.User
import org.mockito.Mockito
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.JsonPathAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

class ControllerTestFactory {

    companion object {

        val APPLICATION_JSON = MediaType.APPLICATION_JSON
        const val AUTHORIZATION = "Authorization"
        const val PREFIX = "Bearer\u0020"

        fun <E : Any, R : ReactiveMongoRepository<E, String>> shouldPostEntity(
                entity: E, repository: R, testClient: WebTestClient, endpoint: String
        ): WebTestClient.ResponseSpec {
            val monoEntity: Mono<E> = Mono.just(entity)
            Mockito.`when`(repository.insert(entity)).thenReturn(monoEntity)
            return testClient.post()
                    .uri(endpoint)
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(monoEntity, entity::class.java)
                    .exchange()
                    .expectStatus().isCreated
        }

        fun <E : Any, R : ReactiveMongoRepository<E, String>> shouldGetEntity(
                id: String, entity: E, repository: R, testClient: WebTestClient, endpoint: String, token: String
        ): WebTestClient.ResponseSpec {

            Mockito.`when`(repository.findById(id)).thenReturn(Mono.just(entity))
            return testClient.get()
                    .uri(endpoint)
                    .header(AUTHORIZATION, PREFIX.plus(token))
                    .accept(APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk

        }

        fun <E : Any, R : ReactiveMongoRepository<E, String>> shouldGetEntities(
                repository: R?, testClient: WebTestClient, endpoint: String, token: String
        ): WebTestClient.ResponseSpec {
            val entityFlux: Flux<E>? = repository?.findAll()
            Mockito.`when`(repository?.findAll()).thenReturn(entityFlux)
            return testClient.get()
                    .uri(endpoint)
                    .header(AUTHORIZATION, PREFIX.plus(token))
                    .accept(APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk
        }

        fun <E : Any, R : ReactiveMongoRepository<E, String>> shouldPatchEntity(
                oldEntity: E, newEntity: E, repository: R, testClient: WebTestClient, endpoint: String, token: String
        ): WebTestClient.ResponseSpec {

            val newEntityMono: Mono<E> = Mono.just(newEntity)
            Mockito.`when`(repository.save(newEntity)).thenReturn(newEntityMono)
            assert(oldEntity != newEntity)
            return testClient.patch()
                    .uri(endpoint)
                    .header(AUTHORIZATION, PREFIX.plus(token))
                    .accept(MediaType.APPLICATION_JSON)
                    .body(newEntityMono, newEntity::class.java)
                    .exchange()
                    .expectStatus().isOk
        }

        fun <E : Any, R : ReactiveMongoRepository<E, String>> shouldDeleteEntity(
                id: String, testClient: WebTestClient, repository: R, endpoint: String, token: String): WebTestClient.ResponseSpec {
            Mockito.`when`(repository.deleteById(id)).thenReturn(Mono.empty())
            return testClient.delete()
                    .uri(endpoint)
                    .header(AUTHORIZATION, PREFIX.plus(token))
                    .accept(APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNoContent
        }

        fun setupWebTestClient(): WebTestClient {
            return WebTestClient
                    .bindToServer()
                    .baseUrl(ControllerPath.BASE_URL)
                    .build()
        }

        fun performSignUpOperation(testClient: WebTestClient, testUser: User) {
            val endpoint = "${ControllerPath.API}${ControllerPath.SALES_DIARY_AUTH_SIGN_UP_USERS}"
            testClient.post().uri(endpoint)
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(Mono.just(testUser), testUser::class.java)
                    .exchange()
        }

        fun performLoginOperation(testClient: WebTestClient, email: String, password: String): JsonPathAssertions {
            val endpoint = "${ControllerPath.API}${ControllerPath.SALES_DIARY_AUTH_LOGIN_USERS}"
            val requestBody: MutableMap<String, String> = mutableMapOf(
                    "email" to email, "password" to password)
            return testClient.post().uri(endpoint).contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(Mono.just(requestBody), MutableMap::class.java)
                    .exchange()
                    .expectBody()
                    .jsonPath("$.token")

        }


        fun <E : Any, R : ReactiveMongoRepository<E, String>> populateReactiveRepository(repo: R, data: List<E>) {
            repo.insert(data)
        }
    }
}