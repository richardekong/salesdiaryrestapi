package com.daveace.salesdiaryrestapi.controllertest

import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADERS
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.populateReactiveRepository
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntities
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntity
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPatchEntity
import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPostEntity
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.repository.ReactiveTraderRepository
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.properties"])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraderControllerTest {

    @MockBean
    private lateinit var traderRepo: ReactiveTraderRepository

    @MockBean
    private lateinit var usrRepo: ReactiveUserRepository
    private lateinit var testClient: WebTestClient
    private lateinit var tokenUtil: TokenUtil
    private lateinit var testUser: User
    private lateinit var testTrader: Trader
    private lateinit var authorizationToken: String

    private fun createTestTraders(): Array<Trader> = arrayOf(
            Trader("test1@mail.net", "John", "test address"),
            Trader("test2@mail.net", "James", "test address"),
            Trader("test3@mail.net", "Jerry", "test address")
    )

    private fun createTestTrader(): Trader {
        return Trader("", "Mickey", "test address")
    }

    @BeforeAll
    fun init() {
        testClient = WebTestClient.bindToServer().baseUrl(BASE_URL).build()
        testUser = usrRepo.findAll().blockFirst()!!
        testTrader = createTestTrader()
        testTrader.email = testUser.email
        testUser.trader = testTrader
        tokenUtil = TokenUtil()
        authorizationToken = tokenUtil.generateToken(testUser)
        populateReactiveRepository(traderRepo, createTestTraders().asList())
    }

    @Test
    @Order(1)
    fun shouldSaveATrader() {
        val endpoint = "$API$SALES_DIARY_TRADERS"
        shouldPostEntity(testTrader, traderRepo, testClient, endpoint)
                .expectBody()
                .jsonPath("$").isNotEmpty.jsonPath("$").isMap
                .jsonPath("$.email").isEqualTo(testTrader.email)
                .jsonPath("$.name").isEqualTo(testTrader.name)
                .jsonPath("$.address").isEqualTo(testTrader.address)
                .jsonPath("$.location").isNotEmpty
                .jsonPath("$.location").isEqualTo(testTrader.location)

    }

    @Test
    @Order(2)
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
    @Order(3)
    fun shouldFindATrader() {
        val endpoint = "$API$SALES_DIARY_TRADER${testTrader.email}"
        shouldGetEntity(testTrader.email, testTrader, traderRepo, testClient, endpoint, authorizationToken)
                .expectBody().jsonPath("$").isNotEmpty
                .jsonPath("$").isMap
                .jsonPath("$.email").isEqualTo(testTrader.email)
                .jsonPath("$.name").isEqualTo(testTrader.name)
                .jsonPath("$.address").isEqualTo(testTrader.address)
                .jsonPath("$.location").isNotEmpty
                .jsonPath("$.location").isEqualTo(testTrader.location)

    }

    @Test
    @Order(4)
    fun shouldUpdateATrader() {
        val newTraderRecord = testTrader.copy()
        newTraderRecord.location = doubleArrayOf(0.83434, 2.0344)
        newTraderRecord.address = "Tester's address"
        val endpoint = "$API$SALES_DIARY_TRADERS?email=${testTrader.email}"
        shouldPatchEntity(testTrader, newTraderRecord, traderRepo, testClient, endpoint, authorizationToken)
                .expectBody().jsonPath("$").isNotEmpty.jsonPath("$").isMap
                .jsonPath("$.location").value<DoubleArray> {
                    assertTrue(!it!!.contentEquals(testTrader.location))
                }
                .jsonPath("$.address").value<String> {
                    assertTrue(it != testTrader.address)
                }
    }

}

