package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.domain.SalesMetrics
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.repository.ReactiveSalesEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@Service
class ReactiveSalesEventServiceImpl : ReactiveSalesEventService {

    @Autowired
    private lateinit var salesEventRepo: ReactiveSalesEventRepository

    @Autowired
    private lateinit var authenticatedUser: AuthenticatedUser

    override fun saveSalesEvent(salesEvent: SalesEvent): Mono<SalesEvent> {
        return authenticatedUser.ownsThisAccountById(salesEvent.traderId)
                .flatMap { salesEventRepo.save(salesEvent) }
    }

    override fun findSalesEvent(id: String): Mono<SalesEvent> {
        return authenticatedUser.ownsThisAccountById(id)
                .flatMap {
                    salesEventRepo.findById(id)
                            .switchIfEmpty(throwAuthenticationException())
                }
    }

    override fun findSalesEvents(): Flux<SalesEvent> {
        return findAllTradersSalesEvents()
    }

    override fun findSalesEvents(date: LocalDate): Flux<SalesEvent> {
        return authenticatedUser.getCurrentUser()
                .flatMapMany { currentUser ->
                    salesEventRepo.findSalesEventsByDate(date)
                            .filter { currentUser.id == it.traderId }
                            .switchIfEmpty(throwAuthenticationException())
                }.switchIfEmpty(throwRestException())
    }

    override fun findDailySalesEvents(): Flux<SalesEvent> {
        return findAllTradersSalesEvents().filter {
            val startTime: LocalDate = LocalDate.now().minusDays(1)
            dateIsBetween(startTime, it.date)
        }.switchIfEmpty(throwRestException())
    }

    override fun findWeeklySalesEvents(): Flux<SalesEvent> {
        return findAllTradersSalesEvents().filter {
            val startTime: LocalDate = LocalDate.now().minusDays(7)
            dateIsBetween(startTime, it.date)
        }.switchIfEmpty(throwRestException())
    }

    override fun findMonthlySalesEvents(): Flux<SalesEvent> {
        return findAllTradersSalesEvents().filter {
            val startTime: LocalDate = LocalDate.now().minusMonths(1)
            dateIsBetween(startTime, it.date)
        }.switchIfEmpty(throwRestException())

    }

    override fun findQuarterlySalesEvents(): Flux<SalesEvent> {
        return findAllTradersSalesEvents().filter {
            val startTime: LocalDate = LocalDate.now().minusMonths(3)
            dateIsBetween(startTime, it.date)
        }.switchIfEmpty(throwRestException())
    }


    override fun findSemesterSalesEvents(): Flux<SalesEvent> {
        return findAllTradersSalesEvents().filter {
            val startTime: LocalDate = LocalDate.now().minusMonths(6)
            dateIsBetween(startTime, it.date)
        }.switchIfEmpty(throwRestException())
    }

    override fun findYearlySalesEvents(): Flux<SalesEvent> {
        return findAllTradersSalesEvents().filter {
            val startTime: LocalDate = LocalDate.now().minusYears(1)
            dateIsBetween(startTime, it.date)
        }.switchIfEmpty(throwRestException())
    }

    override fun findSalesEvents(from: LocalDate, to: LocalDate): Flux<SalesEvent> {
        return findAllTradersSalesEvents().filter { dateIsBetween(from, it.date, to) }
    }

    override fun findSalesEventsMetrics(date: LocalDate): Flux<SalesMetrics> {
        return findSalesEvents(date).collectList().flatMapMany { Flux.just(SalesMetrics(it)) }
    }

    override fun findDailySalesEventsMetrics(): Flux<SalesMetrics> {
        return findDailySalesEvents().collectList().flatMapMany { Flux.just(SalesMetrics(it)) }
    }

    override fun findWeeklySalesEventsMetrics(): Flux<SalesMetrics> {
        return findWeeklySalesEvents().collectList().flatMapMany { Flux.just(SalesMetrics(it)) }
    }

    override fun findMonthlySalesEventsMetrics(): Flux<SalesMetrics> {
        return findMonthlySalesEvents().collectList().flatMapMany { Flux.just(SalesMetrics(it)) }
    }

    override fun findQuarterlySalesEventsMetrics(): Flux<SalesMetrics> {
        return findQuarterlySalesEvents().collectList().flatMapMany { Flux.just(SalesMetrics(it)) }
    }

    override fun findSemesterSalesEventsMetrics(): Flux<SalesMetrics> {
        return findSemesterSalesEvents().collectList().flatMapMany { Flux.just(SalesMetrics(it)) }
    }

    override fun findYearlySalesEventsMetrics(): Flux<SalesMetrics> {
        return findYearlySalesEvents().collectList().flatMapMany { Flux.just(SalesMetrics(it)) }
    }

    override fun findSalesEventsMetrics(from: LocalDate, to: LocalDate): Flux<SalesMetrics> {
        return findSalesEvents(from, to).collectList().flatMapMany { Flux.just(SalesMetrics(it)) }
    }

    private fun findAllTradersSalesEvents(): Flux<SalesEvent> {
        return authenticatedUser.getCurrentUser()
                .flatMapMany { currentUser ->
                    salesEventRepo.findAll()
                            .filter { (currentUser.id == it.traderId) }
                            .switchIfEmpty(throwAuthenticationException())
                }
    }

    private fun <T> throwAuthenticationException(): Mono<T> {
        return Mono.fromRunnable { throw AuthenticationException(HttpStatus.UNAUTHORIZED.reasonPhrase) }
    }

    private fun <T> throwRestException(): Mono<T> {
        return Mono.fromRunnable { throw RestException(HttpStatus.NOT_FOUND.reasonPhrase) }
    }

    private fun dateIsBetween(startDate: LocalDate, providedDate: LocalDate, endDate: LocalDate = LocalDate.now()): Boolean {
        if (startDate.isAfter(endDate).or(startDate.isEqual(endDate)))
            throw RuntimeException("Invalid Date range!")
        return (startDate.isEqual(providedDate).or(startDate.isBefore(providedDate)))
                .and(endDate.isEqual(providedDate).or(endDate.isAfter(providedDate)))
    }

}

