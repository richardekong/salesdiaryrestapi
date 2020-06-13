package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.domain.SalesMetrics
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface ReactiveSalesEventService {

    fun saveSalesEvent(salesEvent: SalesEvent): Mono<SalesEvent>
    fun findSalesEvent(id: String): Mono<SalesEvent>
    fun findSalesEvents(): Flux<SalesEvent>
    fun findSalesEvents(date: LocalDate): Flux<SalesEvent>
    fun findDailySalesEvents(): Flux<SalesEvent>
    fun findWeeklySalesEvents(): Flux<SalesEvent>
    fun findMonthlySalesEvents(): Flux<SalesEvent>
    fun findQuarterlySalesEvents(): Flux<SalesEvent>
    fun findSemesterSalesEvents(): Flux<SalesEvent>
    fun findYearlySalesEvents(): Flux<SalesEvent>
    fun findSalesEvents(from: LocalDate, to: LocalDate): Flux<SalesEvent>
    fun findSalesEventsMetrics(date: LocalDate): Mono<SalesMetrics>
    fun findDailySalesEventsMetrics(): Mono<SalesMetrics>
    fun findWeeklySalesEventsMetrics(): Mono<SalesMetrics>
    fun findMonthlySalesEventsMetrics(): Mono<SalesMetrics>
    fun findQuarterlySalesEventsMetrics(): Mono<SalesMetrics>
    fun findSemesterSalesEventsMetrics(): Mono<SalesMetrics>
    fun findYearlySalesEventsMetrics(): Mono<SalesMetrics>
    fun findSalesEventsMetrics(from: LocalDate, to: LocalDate): Mono<SalesMetrics>

}