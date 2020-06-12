package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.BaseTests
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.domain.SalesMetrics
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


class ReactiveSalesEventServiceTests : BaseTests() {

    @MockBean
    private lateinit var testRepo: ReactiveSalesEventRepository

    @MockBean
    private lateinit var testEventService: ReactiveSalesEventService
    private lateinit var testSalesEvent: SalesEvent

    private fun createTestEvent(): SalesEvent {
        return SalesEvent("TID004", "CID004", "PID004", 3.00, 200.00, 230.00, 5.00, mutableListOf(10.334, 34.084))
    }

    @BeforeAll()
    fun initTest() {
        testSalesEvent = createTestEvent()
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
        val today: LocalDate = LocalDate.now()
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
        val today: LocalDate = LocalDate.now()
        val salesEventMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val salesEventMetricsFlux: Flux<SalesMetrics> = Flux.just(salesEventMetrics)
        Mockito.`when`(testEventService.findSalesEventsMetrics(today)).thenReturn(salesEventMetricsFlux)
        StepVerifier.create(testEventService.findSalesEventsMetrics(today))
                .expectNext(salesEventMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSalesEventsMetrics(today)
    }

    @Test
    @Order(12)
    fun shouldFindDailySalesEventsMetrics() {
        val dailySalesEventsMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val dailySalesEventsMetricsFlux: Flux<SalesMetrics> = Flux.just(dailySalesEventsMetrics)
        Mockito.`when`(testEventService.findDailySalesEventsMetrics()).thenReturn(dailySalesEventsMetricsFlux)
        StepVerifier.create(testEventService.findDailySalesEventsMetrics())
                .expectNext(dailySalesEventsMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findDailySalesEventsMetrics()
    }

    @Test
    @Order(13)
    fun shouldFindWeeklySalesEventsMetrics() {
        val weeklySalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val weeklySalesMetricsFlux: Flux<SalesMetrics> = Flux.just(weeklySalesMetrics)
        Mockito.`when`(testEventService.findWeeklySalesEventsMetrics()).thenReturn(weeklySalesMetricsFlux)
        StepVerifier.create(testEventService.findWeeklySalesEventsMetrics())
                .expectNext(weeklySalesMetrics)
        Mockito.verify(testEventService, times(1)).findWeeklySalesEventsMetrics()
    }

    @Test
    @Order(14)
    fun shouldFindMonthlySalesEventsMetrics() {
        val monthlySalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val monthlySalesMetricsFlux: Flux<SalesMetrics> = Flux.just(monthlySalesMetrics)
        Mockito.`when`(testEventService.findMonthlySalesEventsMetrics()).thenReturn(monthlySalesMetricsFlux)
        StepVerifier.create(testEventService.findMonthlySalesEventsMetrics())
                .expectNext(monthlySalesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findMonthlySalesEventsMetrics()

    }

    @Test
    @Order(15)
    fun shouldFindQuarterSalesEventsMetrics() {
        val quarterSalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val quarterSalesMetricsFlux: Flux<SalesMetrics> = Flux.just(quarterSalesMetrics)
        Mockito.`when`(testEventService.findQuarterlySalesEventsMetrics()).thenReturn(quarterSalesMetricsFlux)
        StepVerifier.create(testEventService.findQuarterlySalesEventsMetrics())
                .expectNext(quarterSalesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findQuarterlySalesEventsMetrics()
    }

    @Test
    @Order(16)
    fun shouldFindSemesterSalesEventsMetrics() {
        val semesterSalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val semesterSalesMetricsFlux: Flux<SalesMetrics> = Flux.just(semesterSalesMetrics)
        Mockito.`when`(testEventService.findSemesterSalesEventsMetrics()).thenReturn(semesterSalesMetricsFlux)
        StepVerifier.create(testEventService.findSemesterSalesEventsMetrics())
                .expectNext(semesterSalesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSemesterSalesEventsMetrics()
    }

    @Test
    @Order(17)
    fun shouldFindYearlySalesEventsMetrics() {
        val yearlySalesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val yearlySalesMetricsFlux: Flux<SalesMetrics> = Flux.just(yearlySalesMetrics)
        Mockito.`when`(testEventService.findYearlySalesEventsMetrics()).thenReturn(yearlySalesMetricsFlux)
        StepVerifier.create(testEventService.findYearlySalesEventsMetrics())
                .expectNext(yearlySalesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findYearlySalesEventsMetrics()
    }

    @Test
    @Order(18)
    fun shouldFindSalesEventsMetricsByDateRange() {

        val from: LocalDate = LocalDate.now().minusDays(Random.nextLong(1, 10))
        val to: LocalDate = LocalDate.now()
        val salesMetrics = SalesMetrics(mutableListOf(testSalesEvent))
        val salesMetricsFlux: Flux<SalesMetrics> = Flux.just(salesMetrics)
        Mockito.`when`(testEventService.findSalesEventsMetrics(from, to)).thenReturn(salesMetricsFlux)
        StepVerifier.create(testEventService.findSalesEventsMetrics(from, to))
                .expectNext(salesMetrics)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSalesEventsMetrics(from, to)
    }

    @Test
    @Order(19)
    fun shouldFindSalesEventByDateRange() {

        val from:LocalDate = LocalDate.now().minusDays(Random.nextLong(1, 10))
        val to:LocalDate = LocalDate.now()
        val salesEventFlux:Flux<SalesEvent> = Flux.just(testSalesEvent)
        Mockito.`when`(testEventService.findSalesEvents(from, to)).thenReturn(salesEventFlux)
        StepVerifier.create(testEventService.findSalesEvents(from, to))
                .expectNext(testSalesEvent)
                .verifyComplete()
        Mockito.verify(testEventService, times(1)).findSalesEvents(from, to)
    }

}