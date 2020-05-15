package com.daveace.salesdiaryrestapi.controllertest

import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_LOGIN_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_RESET_PASSWORD
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_SIGN_UP_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_USERS
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.APPLICATION_JSON
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.populateReactiveRepository
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldDeleteEntity
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntities
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntity
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPatchEntity
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.repository.InMemoryTokenStore
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*


@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.properties"])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @MockBean
    private lateinit var usrRepo: ReactiveUserRepository
    private lateinit var tokenUtil: TokenUtil
    private lateinit var testClient: WebTestClient
    private lateinit var testUser: User
    private lateinit var authorizationToken: String

    private fun createTestUser(): User {
        val email: String = UUID.randomUUID().toString().substring(0, 3).plus("@mail.com")
        return User(email, "test007","0780347")
    }

    private fun createTestUsers(): List<User> {
        return listOf(
                User("test1@mail.com", "test123", "0947344"),
                User("test2@mail.com", "test124",  "0837443"),
                User("test3@mail.com", "test156", "0836453")
        )
    }

    @BeforeAll
    fun init() {
        testClient = WebTestClient.bindToServer().baseUrl(BASE_URL).build()
        testUser = createTestUser()
        tokenUtil = TokenUtil()
        populateReactiveRepository(usrRepo, createTestUsers())
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
                .body(userMono, User::class.java)
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.email").isEqualTo(testUser.email)
                .jsonPath("$.phone").isEqualTo(testUser.phone)
                .jsonPath("$.kind").isEqualTo(testUser.kind)

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
                    InMemoryTokenStore.storeToken(it)
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
    fun shouldUpdateUserAccount() {
        val oldUserToUpdate: User = testUser
        val userToUpdate: User = oldUserToUpdate.copy()
        val email: String = userToUpdate.email
        userToUpdate.phone = "102834834"
        val endpoint = "$API$SALES_DIARY_USER$email"
        shouldPatchEntity(oldUserToUpdate, userToUpdate, usrRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$.phone")
                .isEqualTo(userToUpdate.phone)
    }

    @Test
    @Order(6)
    fun shouldResetUserPassword() {
        val token: String = tokenUtil.generateToken(testUser)
        val newPassword = "test2384"
        val endpoint = "$API$SALES_DIARY_AUTH_RESET_PASSWORD${token}?password=${newPassword}"
        testClient.patch().uri(endpoint)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON).exchange()
                .expectBody()
                .jsonPath("$.password").value<String> {
                    val encoder: PasswordEncoder = BCryptPasswordEncoder()
                    assertTrue(encoder.matches(newPassword, it))
                }

    }

    @Test
    @Order(7)
    fun shouldDeleteUserAccount() {
        testUser = createTestUser()
        shouldSignUpUser()
        shouldLoginUser()
        val email: String = testUser.email
        val endpoint = "$API$SALES_DIARY_USER$email"
        shouldDeleteEntity(email, testClient, usrRepo, endpoint, authorizationToken)
    }

}

