package com.daveace.salesdiaryrestapi.controllertest

import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_LOGIN_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_AUTH_SIGN_UP_USERS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADERS
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.APPLICATION_JSON
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.AUTHORIZATION
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.PREFIX
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntities
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntity
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.repository.InMemoryTokenStore
import com.daveace.salesdiaryrestapi.repository.ReactiveTraderRepository
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
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
class TraderControllerTest {

    @MockBean
    private lateinit var traderRepo: ReactiveTraderRepository
    @MockBean
    private lateinit var usrRepo:ReactiveUserRepository
    private lateinit var testClient: WebTestClient
    private lateinit var tokenUtil: TokenUtil
    private lateinit var testUser: User
    private lateinit var testTrader: Trader
    private lateinit var authorizationToken: String

    private fun createTestUser():User{
        val randomEmail:String = UUID
                .randomUUID().toString()
                .substring(0, 3).plus("@mail.com")
        return User(randomEmail, "test123","09034734632")
    }

    private fun createTestTrader(): Trader {
        return Trader("", "Mickey", "test address")
    }

    private fun performSignUpOperation(client:WebTestClient, user:User){
        val endpoint = "$API$SALES_DIARY_AUTH_SIGN_UP_USERS"
        client.post().uri(endpoint)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(user), user::class.java)
                .exchange()
    }

    private fun performLoginOperation(client:WebTestClient, email:String, password:String){
        val endpoint = "$API$SALES_DIARY_AUTH_LOGIN_USERS"
        val requestBody = mutableMapOf(
                "email" to email, "password" to password)
        client.post().uri(endpoint).contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(Mono.just(requestBody), MutableMap::class.java)
                .exchange()
                .expectBody()
                .jsonPath("$.token").value<String> {
                    authorizationToken = it
                    InMemoryTokenStore.storeToken(it)
                }
    }

    @BeforeAll
    fun init() {
        testClient = WebTestClient.bindToServer().baseUrl(BASE_URL).build()
        testUser = createTestUser()
        testTrader = createTestTrader()
        testTrader.email = testUser.email
        testUser.trader = testTrader
        performSignUpOperation(testClient, testUser)
    }

    @BeforeEach
    fun login(){
        performLoginOperation(testClient, testUser.email, testUser.password)
    }

    @AfterAll
    fun clear(){
        usrRepo.delete(testUser)
        traderRepo.delete(testTrader)
    }

    @Test
    @Order(1)
    fun shouldFindAllTrader() {
        val endpoint = "$API$SALES_DIARY_TRADERS"
        shouldGetEntities(traderRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$").isArray
                .jsonPath("$").isNotEmpty
                .jsonPath("$..links").exists()
                .jsonPath("$..links").isArray
                .jsonPath("$..links[0].rel").exists()
                .jsonPath("$..links[0].href").exists()

    }

    @Test
    @Order(2)
    fun shouldFindATrader() {
        val endpoint = "$API$SALES_DIARY_TRADER${testTrader.email}"
        shouldGetEntity(testTrader.email, testTrader, traderRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$").isMap
                .jsonPath("$.email").isEqualTo(testTrader.email)
                .jsonPath("$.name").isEqualTo(testTrader.name)
                .jsonPath("$.address").isEqualTo(testTrader.address)
                .jsonPath("$.location").isEqualTo(testTrader.location)

    }

}

