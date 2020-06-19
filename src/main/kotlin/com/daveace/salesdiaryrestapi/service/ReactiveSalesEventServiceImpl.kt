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
import java.time.format.DateTimeFormatter

@Service
class ReactiveSalesEventServiceImpl() : ReactiveSalesEventService {

    private lateinit var salesEventRepo: ReactiveSalesEventRepository
    private lateinit var authenticatedUser: AuthenticatedUser
    private lateinit var productService: ReactiveProductService
    private lateinit var traderService: ReactiveTraderService

    companion object {
        private const val DATE_PATTERN = "yyyy-MM-dd"
    }

    @Autowired
    constructor(salesEventRepo: ReactiveSalesEventRepository,
                authenticatedUser: AuthenticatedUser,
                productService: ReactiveProductService,
                traderService: ReactiveTraderService) : this() {

        this.salesEventRepo = salesEventRepo
        this.authenticatedUser = authenticatedUser
        this.productService = productService
        this.traderService = traderService
    }

    override fun saveSalesEvent(salesEvent: SalesEvent): Mono<SalesEvent> {
        return authenticatedUser.getCurrentUser()
                .filter { it.id == salesEvent.traderId }
                .switchIfEmpty(throwAuthenticationException())
                .flatMap { salesEventRepo.save(salesEvent) }
                .doOnSuccess { event ->
                    productService.apply {
                        findProduct(event.productId)
                                .filter { product -> product.stock > 0.0 }
                                .flatMap { product ->
                                    product.stock.dec()
                                    save(product)
                                }.doOnSuccess { product ->
                                    traderService.apply {
                                        updateTraderProduct(event.traderId, product.toMap(), product.id)
                                                .subscribe()
                                    }
                                }.subscribe()
                    }
                }
    }

    override fun findSalesEvent(id: String): Mono<SalesEvent> {
        return authenticatedUser.getCurrentUser()
                .flatMap { currentUser ->
                    salesEventRepo.findById(id)
                            .filter { currentUser.id == it.traderId }
                            .switchIfEmpty(throwAuthenticationException())
                }
    }

    override fun findSalesEvents(): Flux<SalesEvent> {
        return findAllTradersSalesEvents()
    }

    override fun findSalesEvents(dateString: String): Flux<SalesEvent> {
        val date: LocalDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_PATTERN))
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

    override fun findSalesEvents(from: String, to: String): Flux<SalesEvent> {
        val start: LocalDate = LocalDate.parse(from, DateTimeFormatter.ofPattern(DATE_PATTERN))
        val end: LocalDate = LocalDate.parse(to, DateTimeFormatter.ofPattern(DATE_PATTERN))
        return findAllTradersSalesEvents().filter { dateIsBetween(start, it.date, end) }
    }

    override fun findSalesEventsMetrics(dateString: String): Mono<SalesMetrics> {
        return findSalesEvents(dateString).collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.ADHOC.category, it))
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

    override fun findSalesEventsMetrics(from: String, to: String): Mono<SalesMetrics> {
        return findSalesEvents(from, to).collectList().flatMap {
            Mono.just(SalesMetrics(SalesMetrics.Category.ADHOC.category, it))
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

