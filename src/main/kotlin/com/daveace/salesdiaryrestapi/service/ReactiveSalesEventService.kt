package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface ReactiveSalesEventService {

    fun saveSalesEvent(salesEvent: SalesEvent): Mono<SalesEvent>
    fun findSalesEvent(id: String): Mono<SalesEvent>
    fun findSalesEvents(): Flux<SalesEvent>
    fun findSalesEvents(dateString: String): Flux<SalesEvent>
    fun findDailySalesEvents(): Flux<SalesEvent>
    fun findWeeklySalesEvents(): Flux<SalesEvent>
    fun findMonthlySalesEvents(): Flux<SalesEvent>
    fun findQuarterlySalesEvents(): Flux<SalesEvent>
    fun findSemesterSalesEvents(): Flux<SalesEvent>
    fun findYearlySalesEvents(): Flux<SalesEvent>
    fun findSalesEvents(from: String, to: String): Flux<SalesEvent>
    fun findSalesEventsMetrics(currentUser: User):Mono<SalesMetrics>
    fun findSalesEventsMetrics(dateString: String, currentUser:User): Mono<SalesMetrics>
    fun findDailySalesEventsMetrics(currentUser:User): Mono<SalesMetrics>
    fun findWeeklySalesEventsMetrics(currentUser:User): Mono<SalesMetrics>
    fun findMonthlySalesEventsMetrics(currentUser:User): Mono<SalesMetrics>
    fun findQuarterlySalesEventsMetrics(currentUser:User): Mono<SalesMetrics>
    fun findSemesterSalesEventsMetrics(currentUser:User): Mono<SalesMetrics>
    fun findYearlySalesEventsMetrics(currentUser: User): Mono<SalesMetrics>
    fun findSalesEventsMetrics(from: String, to: String, currentUser: User): Mono<SalesMetrics>
    fun findCustomerRetentionMetrics(salesMetrics:SalesMetrics, customers:List<Customer>, purchaseTimes:Int = customers.size):Mono<CustomerRetentionMetrics>

}