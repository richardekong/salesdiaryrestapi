package com.daveace.salesdiaryrestapi.controllertest

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADERS
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.performLoginOperation
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.performSignUpOperation
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntities
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntity
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.repository.ReactiveTraderRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.properties"])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraderControllerTest {

    @MockBean
    private lateinit var traderRepo: ReactiveTraderRepository
    private lateinit var testClient: WebTestClient
    private lateinit var testUser: User
    private lateinit var testTrader: Trader
    private var authorizationToken: String =""

    @BeforeAll
    fun init() {
        testClient = WebTestClient.bindToServer().baseUrl(BASE_URL).build()
        testTrader = createTestTrader()
        testUser = createTestUser(testTrader)
        performSignUpOperation(testClient, testUser)
    }

    @BeforeEach
    fun login(){
        performLoginOperation(testClient, testUser.email, testUser.password)
                .value<String> {
                    if (authorizationToken.isEmpty()){
                        authorizationToken = it
                    }
                }
    }

    private fun createTestUser(trader: Trader): User {
        val user = User(trader.email, "test123", trader.phone)
        user.trader = trader
        return user
    }

    private fun createTestTrader(): Trader {
        val randomEmail: String = UUID.randomUUID().toString()
                .substring(0, 3).plus("@mail.com")
        return Trader(randomEmail, "Mickey", "09034734632","test address")
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
        val traderEmail:String = testTrader.email
        val endpoint = "$API$SALES_DIARY_TRADER$traderEmail"
        shouldGetEntity(testTrader.email, testTrader, traderRepo, testClient, endpoint, authorizationToken)
                .expectBody()
                .jsonPath("$").isMap
                .jsonPath("$.email").isEqualTo(testTrader.email)
                .jsonPath("$.name").isEqualTo(testTrader.name)
                .jsonPath("$.address").isEqualTo(testTrader.address)

    }

}

