package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.BaseTests
import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_LOGIN_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_RESET_PASSWORD
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_SIGN_UP_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.APPLICATION_JSON
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.createWebTestClient
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.shouldDeleteEntity
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.shouldGetEntities
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.shouldGetEntity
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.hateoas.model.UserModel
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*


class UserControllerTest : BaseTests(){

    @MockBean
    lateinit var usrRepo: ReactiveUserRepository
    private lateinit var tokenUtil: TokenUtil
    private lateinit var encoder: PasswordEncoder
    private lateinit var testClient: WebTestClient
    private lateinit var testUser: User
    private lateinit var authorizationToken: String

    @BeforeAll
    fun init() {
        testClient = createWebTestClient()
        encoder = BCryptPasswordEncoder()
        testUser = createTestUser()
        tokenUtil = TokenUtil()
    }

    @Test
    @Order(1)
    fun shouldSignUpUser() {
        val endpoint = "$API$SALES_DIARY_AUTH_SIGN_UP_USERS"
        val userMono: Mono<User> = Mono.just(testUser)
        Mockito.`when`(usrRepo.insert(testUser)).thenReturn(userMono)
        testClient.post()
                .uri(endpoint)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(userMono, UserModel::class.java)
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.email").isEqualTo(testUser.email)
                .jsonPath("$.password").value<String> {
                    assertTrue(encoder.matches(testUser.password, it))
                }

    }

    @Test
    @Order(2)
    fun shouldLoginUser() {
        val endpoint = "$API$SALES_DIARY_AUTH_LOGIN_USERS"
        val password = "password"
        val email = "email"
        val requestBody: MutableMap<String, String> = mutableMapOf(
                email to testUser.email,
                password to testUser.password
        )
        val userMono: Mono<User> = Mono.just(testUser)
        val requestBodyMono = Mono.just(requestBody)
        Mockito.`when`(usrRepo.findById(testUser.email)).thenReturn(userMono)
        testClient.post()
                .uri(endpoint)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(requestBodyMono, MutableMap::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").isMap
                .jsonPath("$.token").exists()
                .jsonPath("$.token").isNotEmpty
                .jsonPath("$.token").value<String> {
                    authorizationToken = it
                }
    }

    @Test
    @Order(3)
    fun shouldFindAUser() {
        val email: String = testUser.email
        val endpoint = "$API$SALES_DIARY_USER$email"
        shouldGetEntity(email, testUser, usrRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$").isNotEmpty
                .jsonPath("$.email").exists()
                .jsonPath("$.email").isEqualTo(email)
    }

    @Test
    @Order(4)
    fun shouldFindAllUsers() {
        val endpoint = "$API$SALES_DIARY_USERS"
        shouldGetEntities(usrRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$").isArray
                .jsonPath("$").isNotEmpty
                .jsonPath("$..links").exists()
                .jsonPath("$..links").isArray
                .jsonPath("$..links[0].rel").exists()
                .jsonPath("$..links[0].href").exists()

    }

    @Test
    @Order(5)
    fun shouldResetUserPassword() {
        val token: String = tokenUtil.generateToken(testUser)
        val newPassword = "test2384"
        val endpoint = "$API$SALES_DIARY_AUTH_RESET_PASSWORD${token}?password=${newPassword}"
        testClient.patch().uri(endpoint)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON).exchange()
                .expectBody()
                .jsonPath("$.password").value<String> {
                    assertTrue(encoder.matches(newPassword, it))
                }

    }

    @Test
    @Order(6)
    fun shouldDeleteUserAccount() {
        testUser = createTestUser()
        shouldSignUpUser()
        shouldLoginUser()
        val email: String = testUser.email
        val endpoint = "$API$SALES_DIARY_USER$email"
        shouldDeleteEntity(email, testClient, usrRepo, endpoint, authorizationToken)
    }

}

