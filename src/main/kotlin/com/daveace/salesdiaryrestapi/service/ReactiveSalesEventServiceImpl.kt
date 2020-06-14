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
class ReactiveSalesEventServiceImpl() : ReactiveSalesEventService {

    private lateinit var salesEventRepo: ReactiveSalesEventRepository
    private lateinit var authenticatedUser: AuthenticatedUser

    @Autowired
    constructor(salesEventRepo: ReactiveSalesEventRepository, authenticatedUser: AuthenticatedUser) : this() {
        this.salesEventRepo = salesEventRepo
        this.authenticatedUser = authenticatedUser
    }

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

    override fun findSalesEventsMetrics(date: LocalDate): Mono<SalesMetrics> {
        return findSalesEvents(date).collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.REGULAR.category, it))
        }
    }

    override fun findDailySalesEventsMetrics(): Mono<SalesMetrics> {
        return findDailySalesEvents().collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.DAILY.category, it))
        }
    }

    override fun findWeeklySalesEventsMetrics(): Mono<SalesMetrics> {
        return findWeeklySalesEvents().collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.WEEKLY.category, it))
        }
    }

    override fun findMonthlySalesEventsMetrics(): Mono<SalesMetrics> {
        return findMonthlySalesEvents().collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.MONTHLY.category, it))
        }
    }

    override fun findQuarterlySalesEventsMetrics(): Mono<SalesMetrics> {
        return findQuarterlySalesEvents().collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.QUARTER.category, it))
        }
    }

    override fun findSemesterSalesEventsMetrics(): Mono<SalesMetrics> {
        return findSemesterSalesEvents().collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.SEMESTER.category, it))
        }
    }

    override fun findYearlySalesEventsMetrics(): Mono<SalesMetrics> {
        return findYearlySalesEvents().collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.YEARLY.category, it))
        }
    }

    override fun findSalesEventsMetrics(from: LocalDate, to: LocalDate): Mono<SalesMetrics> {
        return findSalesEvents(from, to).collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.PERIODIC.category, it))
        }
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

