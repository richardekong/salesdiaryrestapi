package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.domain.SalesMetrics
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.repository.ReactiveSalesEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Service
class ReactiveSalesEventServiceImpl() : ReactiveSalesEventService {

    private lateinit var salesEventRepo: ReactiveSalesEventRepository
    private lateinit var authenticatedUser: AuthenticatedUser
    private lateinit var productService: ReactiveProductService
    private lateinit var traderService: ReactiveTraderService

    companion object {
        private const val DATE_PATTERN = "yyyy-MM-dd"
        private const val PRODUCT_OUT_OF_STOCK = "Product is out of stock!"
        private const val PRODUCT_NOT_ENOUGH = "Product is not enough!"
        private const val INVALID_DATE_RANGE = "Invalid Date range!"
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
        return productService.run {
            findProduct(salesEvent.productId)
                    .filter { it.stock > 0.0 }
                    .switchIfEmpty(Mono.fromRunnable { throw RestException(PRODUCT_OUT_OF_STOCK) })
                    .filter { it.stock >= salesEvent.quantitySold }
                    .switchIfEmpty(Mono.fromRunnable { throw RestException(PRODUCT_NOT_ENOUGH) })
                    .flatMap {
                        it.apply { stock -= salesEvent.quantitySold }
                        save(it)
                        traderService.updateTraderProduct(salesEvent.traderId, it.toMap(), it.id)
                    }
        }.flatMap {
            salesEvent.left = it.stock
            salesEventRepo.save(salesEvent)
        }

    }

    override fun findSalesEvent(id: String): Mono<SalesEvent> {
        return salesEventRepo.findById(id)
    }

    override fun findSalesEvents(): Flux<SalesEvent> {
        return salesEventRepo.findAll()
    }

    override fun findSalesEvents(dateString: String): Flux<SalesEvent> {
        return Mono.just(LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_PATTERN)))
                .flatMapMany {
                    salesEventRepo.findSalesEventsByDate(it)
                }
    }

    override fun findDailySalesEvents(): Flux<SalesEvent> {
        return fetchEventsWithin(LocalDate.now().minusDays(1).toString())
    }

    override fun findWeeklySalesEvents(): Flux<SalesEvent> {
        return fetchEventsWithin(LocalDate.now().minusDays(7).toString())
    }

    override fun findMonthlySalesEvents(): Flux<SalesEvent> {
        return fetchEventsWithin(LocalDate.now().minusMonths(1).toString())

    }

    override fun findQuarterlySalesEvents(): Flux<SalesEvent> {
        return fetchEventsWithin(LocalDate.now().minusMonths(3).toString())
    }

    override fun findSemesterSalesEvents(): Flux<SalesEvent> {
        return fetchEventsWithin(LocalDate.now().minusMonths(6).toString())
    }

    override fun findYearlySalesEvents(): Flux<SalesEvent> {
        return fetchEventsWithin(LocalDate.now().minusYears(1).toString())
    }

    override fun findSalesEvents(from: String, to: String): Flux<SalesEvent> {
        return fetchEventsWithin(from, to)
    }

    override fun findSalesEventsMetrics(dateString: String, currentUser: User): Mono<SalesMetrics> {
        return generateSalesMetrics(findSalesEvents(dateString), currentUser, SalesMetrics.Category.ADHOC.category)
    }

    override fun findDailySalesEventsMetrics(currentUser: User): Mono<SalesMetrics> {
        return generateSalesMetrics(findDailySalesEvents(), currentUser, SalesMetrics.Category.DAILY.category)
    }

    override fun findWeeklySalesEventsMetrics(currentUser: User): Mono<SalesMetrics> {
        return generateSalesMetrics(findWeeklySalesEvents(), currentUser, SalesMetrics.Category.WEEKLY.category)
    }

    override fun findMonthlySalesEventsMetrics(currentUser: User): Mono<SalesMetrics> {
        return generateSalesMetrics(findMonthlySalesEvents(), currentUser, SalesMetrics.Category.MONTHLY.category)
    }

    override fun findQuarterlySalesEventsMetrics(currentUser: User): Mono<SalesMetrics> {
        return generateSalesMetrics(findQuarterlySalesEvents(), currentUser, SalesMetrics.Category.QUARTER.category)
    }

    override fun findSemesterSalesEventsMetrics(currentUser: User): Mono<SalesMetrics> {
        return generateSalesMetrics(findSemesterSalesEvents(), currentUser, SalesMetrics.Category.SEMESTER.category)
    }

    override fun findYearlySalesEventsMetrics(currentUser: User): Mono<SalesMetrics> {
        return generateSalesMetrics(findYearlySalesEvents(), currentUser, SalesMetrics.Category.YEARLY.category)
    }

    override fun findSalesEventsMetrics(from: String, to: String, currentUser: User): Mono<SalesMetrics> {
        return generateSalesMetrics(findSalesEvents(from, to), currentUser, SalesMetrics.Category.ADHOC.category)
    }

    private fun fetchEventsWithin(startDateString: String, endDateString: String = LocalDate.now().toString()): Flux<SalesEvent> {
        val startDate = LocalDate.parse(startDateString, DateTimeFormatter.ofPattern(DATE_PATTERN))
        val endDate = LocalDate.parse(endDateString, DateTimeFormatter.ofPattern(DATE_PATTERN))
        return findSalesEvents().filter { dateIsBetween(startDate, it.date, endDate) }.switchIfEmpty(throwRestException())
    }

    private fun generateSalesMetrics(@NotNull events: Flux<SalesEvent>, @NotNull currentUser: User, @NotBlank category: String): Mono<SalesMetrics> {
        return events.filter { currentUser.id == it.traderId }
                .switchIfEmpty(throwRestException())
                .collectList()
                .flatMap { Mono.just(SalesMetrics(category, it)) }
    }

    private fun <T> throwRestException(): Mono<T> {
        return Mono.fromRunnable { throw RestException(HttpStatus.NOT_FOUND.reasonPhrase) }
    }

    private fun dateIsBetween(startDate: LocalDate, providedDate: LocalDate, endDate: LocalDate = LocalDate.now()): Boolean {
        if (startDate.isAfter(endDate).or(startDate.isEqual(endDate)))
            throw RuntimeException(INVALID_DATE_RANGE)
        return (startDate.isEqual(providedDate).or(startDate.isBefore(providedDate)))
                .and(endDate.isEqual(providedDate).or(endDate.isAfter(providedDate)))
    }

}

