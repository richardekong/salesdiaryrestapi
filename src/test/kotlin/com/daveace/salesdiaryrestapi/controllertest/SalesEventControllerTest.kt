//package com.daveace.salesdiaryrestapi.controllertest
//
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENT
//import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.populateReactiveRepository
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntities
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldGetEntity
//import com.daveace.salesdiaryrestapi.controllertest.ControllerTestFactory.Companion.shouldPostEntity
//import com.daveace.salesdiaryrestapi.domain.SalesEvent
//import com.daveace.salesdiaryrestapi.repository.ReactiveSalesEventRepository
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.junit.jupiter.api.extension.ExtendWith
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import org.springframework.test.web.reactive.server.WebTestClient
//import java.util.UUID.randomUUID
//
//@ExtendWith(SpringExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class SalesEventControllerTest {
//
//    @MockBean
//    private lateinit var eventRepo: ReactiveSalesEventRepository
//    private lateinit var testClient: WebTestClient
//
//    private fun createSalesEvent(): SalesEvent {
//        val productId = randomUUID().toString()
//        val traderId = randomUUID().toString()
//        return SalesEvent(traderId, productId, 20.00, 10.00, 200.00)
//    }
//
//    private fun createSalesEvents(): Array<SalesEvent> {
//        return arrayOf(
//                SalesEvent(randomUUID().toString(), randomUUID().toString(), 50.00, 10.00, 100.00),
//                SalesEvent(randomUUID().toString(), randomUUID().toString(), 20.00, 5.00, 400.00),
//                SalesEvent(randomUUID().toString(), randomUUID().toString(), 30.00, 10.00, 200.00)
//        )
//    }
//
//    @BeforeAll
//    fun init() {
//        testClient = WebTestClient.bindToServer().baseUrl(BASE_URL).build()
//        populateReactiveRepository(eventRepo, createSalesEvents().asList())
//    }
//
//    @Test
//    fun shouldInsertSalesEvent() {
//        val eventToSave = createSalesEvent()
//        val endpoint = API+SALES_DIARY_SALES_EVENTS
//        shouldPostEntity(eventToSave, eventRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$").exists()
//                .jsonPath("$.id").isEqualTo(eventToSave.id)
//                .jsonPath("$.traderId").isEqualTo(eventToSave.traderId)
//                .jsonPath("$.productId").isEqualTo(eventToSave.productId)
//                .jsonPath("$.sales").isEqualTo(eventToSave.sales)
//                .jsonPath("$.left").isEqualTo(eventToSave.left)
//                .jsonPath("$.price").isEqualTo(eventToSave.price)
//                .jsonPath("$._links.self.href").exists()
//                .jsonPath("$._links.self.href").isNotEmpty
//    }
//
//    @Test
//    fun shouldGetSalesEvent() {
//        val eventToRetrieve = createSalesEvent()
//        val id = eventToRetrieve.id
//        val endpoint = "$API$SALES_DIARY_SALES_EVENT$id"
//        shouldGetEntity(id, eventToRetrieve,  eventRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$").exists()
//                .jsonPath("$.id").isEqualTo(eventToRetrieve.id)
//                .jsonPath("$.traderId").isEqualTo(eventToRetrieve.traderId)
//                .jsonPath("$.productId").isEqualTo(eventToRetrieve.productId)
//                .jsonPath("$.sales").isEqualTo(eventToRetrieve.sales)
//                .jsonPath("$.left").isEqualTo(eventToRetrieve.left)
//                .jsonPath("$.price").isEqualTo(eventToRetrieve.price)
//                .jsonPath("$._links.self.href").exists()
//                .jsonPath("$._links.self.href").isNotEmpty
//    }
//
//    @Test
//    fun shouldGetSalesEvents() {
//        val eventFlux = eventRepo.findAll()
//        val endpoint = "$API$SALES_DIARY_SALES_EVENTS"
//        shouldGetEntities(eventFlux, eventRepo, testClient, endpoint)
//                .expectBody()
//                .jsonPath("$").isArray
//                .jsonPath("$").isNotEmpty
//                .jsonPath("$..links").exists()
//                .jsonPath("$..links").isArray
//                .jsonPath("$..links[0].rel").exists()
//                .jsonPath("$..links[0].href").exists()
//    }
//
//}