package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.BaseTests
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.BASE_URL
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SEMESTER_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SEMESTER_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_YEARLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_YEARLY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.createWebTestClient
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.performDeleteOperation
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.performLoginOperation
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.performSignUpOperation
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.shouldGetResponse
import com.daveace.salesdiaryrestapi.controller.ControllerTestFactory.Companion.shouldPostEntity
import com.daveace.salesdiaryrestapi.domain.*
import com.daveace.salesdiaryrestapi.hateoas.model.SalesEventModel
import com.daveace.salesdiaryrestapi.service.ReactiveSalesEventServiceImpl
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.hateoas.Link
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import kotlin.random.Random

class SalesEventControllerTest : BaseTests() {

    @MockBean
    private lateinit var eventService: ReactiveSalesEventServiceImpl
    private lateinit var testClient: WebTestClient
    private lateinit var testUser: User
    private lateinit var testTrader: Trader
    private lateinit var testProduct: Product
    private lateinit var testCustomer: Customer
    private lateinit var testEvent: SalesEvent
    private var authToken: String = ""
    private val defaultQueryParameterString = "?size=1&page=0&sort=id&dir=asc"

    @BeforeAll
    fun init() {
        testClient = createWebTestClient()
        testUser = createTestUser()
        testTrader = createTestTrader(testUser)
        testProduct = createTestProduct(testTrader)
        testCustomer = createTestCustomer(testTrader)
        testEvent = createTestEvent(testTrader, testProduct, testCustomer)
        performSignUpOperation(testClient, testUser)
    }

    @BeforeEach
    fun login() {
        performLoginOperation(testClient, testUser.email, testUser.password)
                .value<String> { token ->
                    if (authToken.isEmpty()) authToken = token
                }
    }

    @AfterAll
    fun clearUser() {
        performDeleteOperation(testClient, testUser.email, authToken)
    }

    private fun createLinksFrom(endpoint: String): List<Link> {
        val href = "$BASE_URL$endpoint"
        return listOf(
                Link.of(href, "first"),
                Link.of(href, "last"),
                Link.of(href, "self")
        )
    }

    @Test
    @Order(1)
    fun shouldPostSalesEvent() {
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS"
        val expectEventMono: Mono<SalesEvent> = Mono.just(testEvent)
        `when`(eventService.saveSalesEvent(testEvent)).thenReturn(expectEventMono)
        shouldPostEntity(SalesEventModel(testEvent), expectEventMono, testClient, endpoint, authToken)
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.id").isEqualTo(testEvent.id)
                .jsonPath("$.traderId").isEqualTo(testEvent.traderId)
                .jsonPath("$.customerId").isEqualTo(testEvent.customerId)
                .jsonPath("$.productId").isEqualTo(testEvent.productId)
                .jsonPath("$.costPrice").isEqualTo(testEvent.costPrice)
                .jsonPath("$.salesPrice").isEqualTo(testEvent.salesPrice)
    }

    @Test
    @Order(2)
    fun shouldGetSalesEvents() {
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS$defaultQueryParameterString"
        val expectedEventFlux: Flux<SalesEvent> = Flux.just(testEvent)
        `when`(eventService.findSalesEvents()).thenReturn(expectedEventFlux)
        val responseFlux: Flux<SalesEventModel> = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk
                .returnResult(SalesEventModel::class.java)
                .responseBody
        val expectedEventModel: SalesEventModel = SalesEventModel(testEvent)
                .add(createLinksFrom(endpoint))
        StepVerifier.create(responseFlux)
                .expectNext(expectedEventModel)
                .verifyComplete()

    }

    @Test
    @Order(3)
    fun shouldGetSalesEventsByDate() {
        val dateString: String = LocalDate.now().toString()
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS/dates$defaultQueryParameterString&date=$dateString"
        val expectedEventFlux: Flux<SalesEvent> = Flux.just(testEvent)
        `when`(eventService.findSalesEvents(dateString)).thenReturn(expectedEventFlux)
        val responseFlux: Flux<SalesEventModel> = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk
                .returnResult(SalesEventModel::class.java)
                .responseBody
        val expectedEventModel: SalesEventModel = SalesEventModel(testEvent)
                .add(createLinksFrom(endpoint))
        StepVerifier.create(responseFlux)
                .expectNext(expectedEventModel)
                .verifyComplete()

    }

    @Test
    @Order(4)
    fun shouldGetDailySalesEvents() {
        val endpoint = "$API$SALES_DIARY_DAILY_SALES_EVENTS$defaultQueryParameterString"
        val expectDailyEventsFlux: Flux<SalesEvent> = Flux.just(testEvent)
        `when`(eventService.findDailySalesEvents()).thenReturn(expectDailyEventsFlux)
        val responseBodyFlux: Flux<SalesEventModel> = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk
                .returnResult(SalesEventModel::class.java)
                .responseBody
        val expectedEventModel: SalesEventModel = SalesEventModel(testEvent)
                .add(createLinksFrom(endpoint))
        StepVerifier.create(responseBodyFlux)
                .expectNext(expectedEventModel)
                .verifyComplete()
    }

    @Test
    @Order(5)
    fun shouldGetWeeklySalesEvents() {
        val endpoint = "$API$SALES_DIARY_WEEKLY_SALES_EVENTS$defaultQueryParameterString"
        val expectedWeeklyEventFlux = Flux.just(testEvent)
        `when`(eventService.findWeeklySalesEvents()).thenReturn(expectedWeeklyEventFlux)
        val responseBodyFlux = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk
                .returnResult(SalesEventModel::class.java)
                .responseBody
        val expectEventModel = SalesEventModel(testEvent).add(createLinksFrom(endpoint))
        StepVerifier.create(responseBodyFlux)
                .expectNext(expectEventModel)
                .verifyComplete()
    }

    @Test
    @Order(6)
    fun shouldGetMonthlySalesEvents() {
        val endpoint = "$API$SALES_DIARY_MONTHLY_SALES_EVENTS$defaultQueryParameterString"
        val expectedMonthlyEventFlux = Flux.just(testEvent)
        `when`(eventService.findMonthlySalesEvents()).thenReturn(expectedMonthlyEventFlux)
        val responseBodyFlux = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk
                .returnResult(SalesEventModel::class.java)
                .responseBody
        val expectedEventModel = SalesEventModel(testEvent).add(createLinksFrom(endpoint))
        StepVerifier.create(responseBodyFlux)
                .expectNext(expectedEventModel)
                .verifyComplete()
    }

    @Test
    @Order(7)
    fun shouldGetQuarterlySalesEvent() {
        val endpoint = "$API$SALES_DIARY_QUARTERLY_SALES_EVENTS$defaultQueryParameterString"
        val expectedQuarterEventFlux = Flux.just(testEvent)
        `when`(eventService.findQuarterlySalesEvents()).thenReturn(expectedQuarterEventFlux)
        val responseBodyFlux = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk
                .returnResult(SalesEventModel::class.java)
                .responseBody
        val expectEventModel = SalesEventModel(testEvent).add(createLinksFrom(endpoint))
        StepVerifier.create(responseBodyFlux)
                .expectNext(expectEventModel)
                .verifyComplete()
    }

    @Test
    @Order(8)
    fun shouldGetSemesterSalesEvents() {
        val endpoint = "$API$SALES_DIARY_SEMESTER_SALES_EVENTS$defaultQueryParameterString"
        val expectedSemesterEventFlux = Flux.just(testEvent)
        `when`(eventService.findSemesterSalesEvents()).thenReturn(expectedSemesterEventFlux)
        val responseBodyFLux = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk
                .returnResult(SalesEventModel::class.java)
                .responseBody
        val expectedEventModel = SalesEventModel(testEvent).add(createLinksFrom(endpoint))
        StepVerifier.create(responseBodyFLux)
                .expectNext(expectedEventModel)
                .verifyComplete()
    }

    @Test
    @Order(9)
    fun shouldGetYearlySalesEvents() {
        val endpoint = "$API$SALES_DIARY_YEARLY_SALES_EVENTS$defaultQueryParameterString"
        val expectedYearlyEventsFlux = Flux.just(testEvent)
        `when`(eventService.findYearlySalesEvents()).thenReturn(expectedYearlyEventsFlux)
        val responseBodyFlux = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk
                .returnResult(SalesEventModel::class.java)
                .responseBody
        StepVerifier.create(responseBodyFlux)
                .expectNext(SalesEventModel(testEvent).add(createLinksFrom(endpoint)))
                .verifyComplete()
    }

    @Test
    @Order(10)
    fun shouldGetSalesEventsByDateRange() {
        val from: String = LocalDate.now().minusDays(1).toString()
        val to: String = LocalDate.now().toString()
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS/period$defaultQueryParameterString&from=$from&to=$to"
        val expectedAdhocEventsFlux = Flux.just(testEvent)
        `when`(eventService.findSalesEvents(from, to)).thenReturn(expectedAdhocEventsFlux)
        val responseBodyFlux = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk.returnResult(SalesEventModel::class.java)
                .responseBody
        StepVerifier.create(responseBodyFlux)
                .expectNext(SalesEventModel(testEvent).add(createLinksFrom(endpoint)))
                .verifyComplete()
    }

    @Test
    @Order(11)
    fun shouldGetSalesEvent() {
        val id = testEvent.id
        val endpoint = "$API$SALES_DIARY_SALES_EVENT${id}"
        val expectedEventMono = Mono.just(testEvent)
        `when`(eventService.findSalesEvent(id)).thenReturn(expectedEventMono)
        val responseBodyFlux = shouldGetResponse(testClient, endpoint, authToken)
                .expectStatus().isOk.returnResult(SalesEventModel::class.java)
                .responseBody
        StepVerifier.create(responseBodyFlux)
                .expectNext(SalesEventModel(testEvent).add(Link.of("$BASE_URL$endpoint", "self")))
                .verifyComplete()

    }

    @Test
    @Order(12)
    fun shouldGetSalesEventMetricsByDate() {
        val dateString: String = LocalDate.now().toString()
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS_METRICS/$dateString"
        val expectedSalesMetrics = SalesMetrics(SalesMetrics.Category.ADHOC.category, mutableListOf(testEvent))
        val expectedSalesMetricsMono: Mono<SalesMetrics> = Mono.just(expectedSalesMetrics)
        `when`(eventService.findSalesEventsMetrics(dateString)).thenReturn(expectedSalesMetricsMono)
        testResponseForSalesMetrics(endpoint, expectedSalesMetrics)
    }

    @Test
    @Order(13)
    fun shouldGetDailySalesEventsMetrics() {
        val endpoint = "$API$SALES_DIARY_DAILY_SALES_EVENTS_METRICS"
        val expectedSalesMetrics = SalesMetrics(SalesMetrics.Category.DAILY.category, mutableListOf(testEvent))
        val expectedSalesMetricsMono: Mono<SalesMetrics> = Mono.just(expectedSalesMetrics)
        `when`(eventService.findDailySalesEventsMetrics()).thenReturn(expectedSalesMetricsMono)
        testResponseForSalesMetrics(endpoint, expectedSalesMetrics)
    }

    @Test
    @Order(14)
    fun shouldGetWeeklySalesEventsMetrics() {
        val endpoint = "$API$SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS"
        val expectedSalesMetrics = SalesMetrics(SalesMetrics.Category.WEEKLY.category, mutableListOf(testEvent))
        val expectedSalesMetricsMono: Mono<SalesMetrics> = Mono.just(expectedSalesMetrics)
        `when`(eventService.findWeeklySalesEventsMetrics()).thenReturn(expectedSalesMetricsMono)
        testResponseForSalesMetrics(endpoint, expectedSalesMetrics)

    }

    @Test
    @Order(15)
    fun shouldGetMonthlySalesEventsMetrics() {
        val endpoint = "$API$SALES_DIARY_MONTHLY_SALES_EVENTS_METRICS"
        val expectedSalesMetrics = SalesMetrics(SalesMetrics.Category.MONTHLY.category, mutableListOf(testEvent))
        val expectedSalesMetricsMono: Mono<SalesMetrics> = Mono.just(expectedSalesMetrics)
        `when`(eventService.findMonthlySalesEventsMetrics()).thenReturn(expectedSalesMetricsMono)
        testResponseForSalesMetrics(endpoint, expectedSalesMetrics)
    }

    @Test
    @Order(16)
    fun shouldGetQuarterlySalesEventsMetrics() {
        val endpoint = "$API$SALES_DIARY_QUARTERLY_SALES_EVENTS_METRICS"
        val expectedSalesMetrics = SalesMetrics(SalesMetrics.Category.QUARTER.category, mutableListOf(testEvent))
        val expectedSalesMetricsMono: Mono<SalesMetrics> = Mono.just(expectedSalesMetrics)
        `when`(eventService.findQuarterlySalesEventsMetrics()).thenReturn(expectedSalesMetricsMono)
        testResponseForSalesMetrics(endpoint, expectedSalesMetrics)

    }

    @Test
    @Order(17)
    fun shouldGetSemesterSalesEventsMetrics() {
        val endpoint = "$API$SALES_DIARY_SEMESTER_SALES_EVENTS_METRICS"
        val expectedSalesMetrics = SalesMetrics(SalesMetrics.Category.SEMESTER.category, mutableListOf(testEvent))
        val expectedSalesMetricsMono: Mono<SalesMetrics> = Mono.just(expectedSalesMetrics)
        `when`(eventService.findSemesterSalesEventsMetrics()).thenReturn(expectedSalesMetricsMono)
        testResponseForSalesMetrics(endpoint, expectedSalesMetrics)
    }

    @Test
    @Order(18)
    fun shouldGetYearlySalesEventsMetrics() {
        val endpoint = "$API$SALES_DIARY_YEARLY_SALES_EVENTS_METRICS"
        val expectedSalesMetrics = SalesMetrics(SalesMetrics.Category.YEARLY.category, mutableListOf(testEvent))
        val expectedSalesMetricsMono: Mono<SalesMetrics> = Mono.just(expectedSalesMetrics)
        `when`(eventService.findYearlySalesEventsMetrics()).thenReturn(expectedSalesMetricsMono)
        testResponseForSalesMetrics(endpoint, expectedSalesMetrics)
    }

    @Test
    @Order(19)
    fun shouldGetSalesEventsMetricsByDateRange() {
        val from: String = LocalDate.now().minusDays(Random.nextLong(1, 10)).toString()
        val to: String = LocalDate.now().toString()
        val endpoint = "$API$SALES_DIARY_SALES_EVENTS_METRICS/period?from=$from&to=$to"
        val expectedSalesMetrics = SalesMetrics(SalesMetrics.Category.ADHOC.category, mutableListOf(testEvent))
        val expectedSalesMetricsMono: Mono<SalesMetrics> = Mono.just(expectedSalesMetrics)
        `when`(eventService.findSalesEventsMetrics(from, to)).thenReturn(expectedSalesMetricsMono)
        testResponseForSalesMetrics(endpoint, expectedSalesMetrics)
    }

    private fun testResponseForSalesMetrics(endpoint: String, expectedSalesMetrics: SalesMetrics) {
        shouldGetResponse(testClient, endpoint, authToken).expectStatus().isOk.expectBody()
                .jsonPath("$.category").isEqualTo(expectedSalesMetrics.category)
                .jsonPath("$.totalCost").isEqualTo(expectedSalesMetrics.totalCost)
                .jsonPath("$.totalSales").isEqualTo(expectedSalesMetrics.totalSales)
                .jsonPath("$.totalProfit").isEqualTo(expectedSalesMetrics.totalProfit)
    }

}

