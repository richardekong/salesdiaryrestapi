//package com.daveace.salesdiaryrestapi.controllertest
//
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADER
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADERS
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.populateReactiveRepository
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntities
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntity
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPatchEntity
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPostEntity
//import com.daveace.salesdiaryrestapi.domain.Trader
//import com.daveace.salesdiaryrestapi.repository.ReactiveTraderRepository
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.junit.jupiter.api.extension.ExtendWith
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.data.geo.Point
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import org.springframework.test.web.reactive.server.WebTestClient
//import java.util.*
//
//@ExtendWith(SpringExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class TraderControllerTest {
//
//    @MockBean
//    private lateinit var traderRepo: ReactiveTraderRepository
//    private lateinit var testClient: WebTestClient
//
//    private fun createTestTraders(): Array<Trader> = arrayOf(
//            Trader("test1@mail.net", "John", "test address"),
//            Trader("test2@mail.net", "James", "test address"),
//            Trader("test3@mail.net", "Jerry", "test address")
//    )
//
//    private fun createTestTrader(): Trader {
//        val traderEmail = UUID.randomUUID().toString().substring(0,3).plus("@mail.net")
//        return Trader(traderEmail, "Mickey", "test address")
//    }
//
//    @BeforeAll
//    fun init() {
//        testClient = WebTestClient.bindToServer().baseUrl(BASE_URL).build()
//        populateReactiveRepository(traderRepo, createTestTraders().asList())
//    }
//
//    @Test
//    fun shouldSaveATrader() {
//        val traderToSave = createTestTrader()
//        val endpoint = "$API$SALES_DIARY_TRADERS"
//        shouldPostEntity(traderToSave, traderRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$").isNotEmpty.jsonPath("$").isMap
//                .jsonPath("$.email").isEqualTo(traderToSave.email)
//                .jsonPath("$.name").isEqualTo(traderToSave.name)
//                .jsonPath("$.address").isEqualTo(traderToSave.address)
//                .jsonPath("$.location").isNotEmpty
//                .jsonPath("$.location").isEqualTo(traderToSave.location)
//                .jsonPath("$._links.self.href").exists()
//                .jsonPath("$._links.self.href").isNotEmpty
//
//    }
//
//    @Test
//    fun shouldFindAllTrader() {
//        val endpoint = "$API$SALES_DIARY_TRADERS"
//        val traderFlux = traderRepo.findAll()
//        shouldGetEntities(traderFlux, traderRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$").isArray
//                .jsonPath("$").isNotEmpty
//                .jsonPath("$.links").isArray
//                .jsonPath("$.links[0].rel").exists()
//                .jsonPath("$.links[0].href").exists()
//
//    }
//
//    @Test
//    fun shouldFindATrader() {
//        val testTrader = createTestTrader()
//        val endpoint = "$API$SALES_DIARY_TRADER${testTrader.email}"
//        shouldGetEntity(testTrader.email, testTrader, traderRepo, testClient, endpoint)
//                .expectBody().jsonPath("$").isNotEmpty
//                .jsonPath("$").isMap
//                .jsonPath("$.email").isEqualTo(testTrader.email)
//                .jsonPath("$.name").isEqualTo(testTrader.name)
//                .jsonPath("$.address").isEqualTo(testTrader.address)
//                .jsonPath("$.location").isNotEmpty
//                .jsonPath("$.location").isEqualTo(testTrader.location)
//                .jsonPath("$._links.self.href").exists()
//                .jsonPath("$._links.self.href").isNotEmpty
//
//    }
//
//    @Test
//    fun shouldUpdateATrader() {
//        val oldTraderRecord = createTestTrader()
//        val newTraderRecord = oldTraderRecord.copy()
//        newTraderRecord.location = Point(0.83434, 2.0344)
//        newTraderRecord.address = "Tester's address"
//        val endpoint = "$API$SALES_DIARY_TRADERS?email=${oldTraderRecord.email}"
//        shouldPatchEntity(oldTraderRecord, newTraderRecord, traderRepo, testClient,endpoint)
//                .expectBody().jsonPath("$").isNotEmpty.jsonPath("$").isMap
//                .jsonPath("$.location != '${oldTraderRecord.location}'").isEqualTo(true)
//                .jsonPath("$.address != '${oldTraderRecord.address}'").isEqualTo(true)
//    }
//
//}
//
