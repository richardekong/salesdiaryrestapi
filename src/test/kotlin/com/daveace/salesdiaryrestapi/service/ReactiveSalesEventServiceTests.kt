package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.BaseTests
import com.daveace.salesdiaryrestapi.domain.*
import com.daveace.salesdiaryrestapi.repository.ReactiveSalesEventRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import kotlin.random.Random


class ReactiveSalesEventServiceTests :BaseTests(){

    @MockBean
    private lateinit var testRepo: ReactiveSalesEventRepository

    @MockBean
    private lateinit var testEventService: ReactiveSalesEventService
    private lateinit var testUser: User
    private lateinit var testTrader:Trader
    private lateinit var testCustomer:Customer
    private lateinit var testSalesEvent: SalesEvent
    private lateinit var testProduct:Product

    @BeforeAll()
    fun initTest() {
        testUser = createTestUser()
        testTrader = createTestTrader(testUser)
        testCustomer = createTestCustomer(testTrader)
        testProduct = createTestProduct(testTrader)
        testSalesEvent = createTestEvent(testTrader, testProduct, testCustomer)
    }

    @AfterAll()
    fun clearRepository() {
        Mockito.`when`(testRepo.deleteAll()).thenReturn(Mono.empty())
        testRepo.deleteAll()
    }

    @Test
    @Order(1)
    fun shouldSaveSalesEvent() {
        val salesEventMono: Mono<SalesEvent> = Mono.just(testSalesEvent)
        Mockito.`when`(testEventService.saveSalesEvent(testSalesEvent))
                .thenReturn(salesEventMono)
        StepVerifier.create(testEventService.saveSalesEvent(testSalesEvent))
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).saveSalesEvent(testSalesEvent)
    }

    @Test
    @Order(2)
    fun shouldFindSalesEventById() {
        val id: String = testSalesEvent.id
        val salesEventMono: Mono<SalesEvent> = Mono.just(testSalesEvent)
        Mockito.`when`(testEventService.findSalesEvent(id))
                .thenReturn(salesEventMono)
        StepVerifier.create(testEventService.findSalesEvent(id))
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSalesEvent(id)
    }

    @Test
    @Order(3)
    fun shouldFindSalesEvents() {
        val salesEventFlux: Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findSalesEvents()).thenReturn(salesEventFlux)
        StepVerifier.create(testEventService.findSalesEvents())
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSalesEvents()
    }

    @Test
    @Order(4)
    fun shouldFindSalesEventsByDate() {
        val today: String = LocalDate.now().toString()
        val salesEventFlux: Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findSalesEvents(today)).thenReturn(salesEventFlux)
        StepVerifier.create(testEventService.findSalesEvents(today))
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSalesEvents(today)
    }

    @Test
    @Order(5)
    fun shouldFindDailySalesEvents() {
        val dailySalesEventsFlux: Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findDailySalesEvents()).thenReturn(dailySalesEventsFlux)
        StepVerifier.create(testEventService.findDailySalesEvents())
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findDailySalesEvents()
    }

    @Test
    @Order(6)
    fun shouldFindWeeklySalesEvents() {
        val weeklySalesEventsFlux: Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findWeeklySalesEvents()).thenReturn(weeklySalesEventsFlux)
        StepVerifier.create(testEventService.findWeeklySalesEvents())
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findWeeklySalesEvents()
    }

    @Test
    @Order(7)
    fun shouldFindMonthlySalesEvents() {
        val monthlySalesEventsFlux: Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findMonthlySalesEvents()).thenReturn(monthlySalesEventsFlux)
        StepVerifier.create(testEventService.findMonthlySalesEvents())
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findMonthlySalesEvents()
    }

    @Test
    @Order(8)
    fun shouldFindQuarterlySalesEvents() {
        val quarterSalesEvents: Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findQuarterlySalesEvents()).thenReturn(quarterSalesEvents)
        StepVerifier.create(testEventService.findQuarterlySalesEvents())
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findQuarterlySalesEvents()
    }

    @Test
    @Order(9)
    fun shouldFindSemesterSalesEvents() {
        val semesterSalesEventsFlux: Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findSemesterSalesEvents()).thenReturn(semesterSalesEventsFlux)
        StepVerifier.create(testEventService.findSemesterSalesEvents())
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSemesterSalesEvents()
    }

    @Test
    @Order(10)
    fun shouldFindYearlySalesEvents() {
        val yearlySalesEventsFlux: Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findYearlySalesEvents()).thenReturn(yearlySalesEventsFlux)
        StepVerifier.create(testEventService.findYearlySalesEvents())
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findYearlySalesEvents()
    }

    @Test
    @Order(11)
    fun shouldFindSalesEventsMetrics() {
        val today: String = LocalDate.now().toString()
        val salesEventMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val salesEventMetricsMono: Mono<SalesMetrics> = Mono.just(salesEventMetrics)
        Mockito.`when`(testEventService.findSalesEventsMetrics(today, testUser)).thenReturn(salesEventMetricsMono)
        StepVerifier.create(testEventService.findSalesEventsMetrics(today, testUser))
                .expectNext(salesEventMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSalesEventsMetrics(today, testUser)
    }

    @Test
    @Order(12)
    fun shouldFindDailySalesEventsMetrics() {
        val dailySalesEventsMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val dailySalesEventsMetricsMono: Mono<SalesMetrics> = Mono.just(dailySalesEventsMetrics)
        Mockito.`when`(testEventService.findDailySalesEventsMetrics(testUser)).thenReturn(dailySalesEventsMetricsMono)
        StepVerifier.create(testEventService.findDailySalesEventsMetrics(testUser))
                .expectNext(dailySalesEventsMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findDailySalesEventsMetrics(testUser)
    }

    @Test
    @Order(13)
    fun shouldFindWeeklySalesEventsMetrics() {
        val weeklySalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val weeklySalesMetricsMono: Mono<SalesMetrics> = Mono.just(weeklySalesMetrics)
        Mockito.`when`(testEventService.findWeeklySalesEventsMetrics(testUser)).thenReturn(weeklySalesMetricsMono)
        StepVerifier.create(testEventService.findWeeklySalesEventsMetrics(testUser))
                .expectNext(weeklySalesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findWeeklySalesEventsMetrics(testUser)
    }

    @Test
    @Order(14)
    fun shouldFindMonthlySalesEventsMetrics() {
        val monthlySalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val monthlySalesMetricsMono: Mono<SalesMetrics> = Mono.just(monthlySalesMetrics)
        Mockito.`when`(testEventService.findMonthlySalesEventsMetrics(testUser)).thenReturn(monthlySalesMetricsMono)
        StepVerifier.create(testEventService.findMonthlySalesEventsMetrics(testUser))
                .expectNext(monthlySalesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findMonthlySalesEventsMetrics(testUser)

    }

    @Test
    @Order(15)
    fun shouldFindQuarterSalesEventsMetrics() {
        val quarterSalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val quarterSalesMetricsMono: Mono<SalesMetrics> = Mono.just(quarterSalesMetrics)
        Mockito.`when`(testEventService.findQuarterlySalesEventsMetrics(testUser)).thenReturn(quarterSalesMetricsMono)
        StepVerifier.create(testEventService.findQuarterlySalesEventsMetrics(testUser))
                .expectNext(quarterSalesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findQuarterlySalesEventsMetrics(testUser)
    }

    @Test
    @Order(16)
    fun shouldFindSemesterSalesEventsMetrics() {
        val semesterSalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val semesterSalesMetricsMono: Mono<SalesMetrics> = Mono.just(semesterSalesMetrics)
        Mockito.`when`(testEventService.findSemesterSalesEventsMetrics(testUser)).thenReturn(semesterSalesMetricsMono)
        StepVerifier.create(testEventService.findSemesterSalesEventsMetrics(testUser))
                .expectNext(semesterSalesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSemesterSalesEventsMetrics(testUser)
    }

    @Test
    @Order(17)
    fun shouldFindYearlySalesEventsMetrics() {
        val yearlySalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val yearlySalesMetricsMono: Mono<SalesMetrics> = Mono.just(yearlySalesMetrics)
        Mockito.`when`(testEventService.findYearlySalesEventsMetrics(testUser)).thenReturn(yearlySalesMetricsMono)
        StepVerifier.create(testEventService.findYearlySalesEventsMetrics(testUser))
                .expectNext(yearlySalesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findYearlySalesEventsMetrics(testUser)
    }

    @Test
    @Order(18)
    fun shouldFindSalesEventsMetricsByDateRange() {

        val from: String = LocalDate.now().minusDays(Random.nextLong(1, 10)).toString()
        val to: String = LocalDate.now().toString()
        val salesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val salesMetricsMono: Mono<SalesMetrics> = Mono.just(salesMetrics)
        Mockito.`when`(testEventService.findSalesEventsMetrics(from, to, testUser)).thenReturn(salesMetricsMono)
        StepVerifier.create(testEventService.findSalesEventsMetrics(from, to, testUser))
                .expectNext(salesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSalesEventsMetrics(from, to, testUser)
    }

    @Test
    @Order(19)
    fun shouldFindSalesEventByDateRange() {

        val from:String = LocalDate.now().minusDays(Random.nextLong(1, 10)).toString()
        val to:String = LocalDate.now().toString()
        val salesEventFlux:Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findSalesEvents(from, to)).thenReturn(salesEventFlux)
        StepVerifier.create(testEventService.findSalesEvents(from, to))
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSalesEvents(from, to)
    }

}