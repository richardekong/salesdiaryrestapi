package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS_EXTENDED_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENTS_EXTENDED_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENTS_EXTENDED_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS_EXTENDED_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SEMESTER_SALES_EVENTS_EXTENDED_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SEMESTER_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS_EXTENDED_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_YEARLY_SALES_EVENTS_EXTENDED_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_YEARLY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.domain.*
import com.daveace.salesdiaryrestapi.mapper.Mappable
import com.daveace.salesdiaryrestapi.report.StockDialImageGenerator
import com.daveace.salesdiaryrestapi.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal

@RestController
@RequestMapping(API)
class ReportDownloadController : BaseController() {

    private lateinit var eventService: ReactiveSalesEventService
    private lateinit var reportService: ReactiveSalesReportService
    private lateinit var customerService: ReactiveCustomerService
    private lateinit var productService: ReactiveProductService
    private lateinit var expenditureService: ReactiveExpenditureService
    private lateinit var creditService: ReactiveCreditService
    private lateinit var traderService: ReactiveTraderService

    @Autowired
    fun initEventService(eventService: ReactiveSalesEventService) {
        this.eventService = eventService
    }

    @Autowired
    fun initReportService(reportService: ReactiveSalesReportService) {
        this.reportService = reportService
    }

    @Autowired
    fun initCustomerService(customerService: ReactiveCustomerService) {
        this.customerService = customerService
    }

    @Autowired
    fun initProductService(productService: ReactiveProductService) {
        this.productService = productService
    }

    @Autowired
    fun initTraderService(traderService: ReactiveTraderService) {
        this.traderService = traderService
    }

    @Autowired
    fun initExpenditureService(expenditureService: ReactiveExpenditureService) {
        this.expenditureService = expenditureService
    }

    @Autowired
    fun initCreditService(creditService: ReactiveCreditService) {
        this.creditService = creditService
    }

    @GetMapping(SALES_DIARY_SALES_EVENTS_REPORT)
    fun generateAllSalesEvents(
        swe: ServerWebExchange,
        principal: Principal
    ): Mono<ResponseEntity<InputStreamResource>> {

        return generateSalesEventReport(swe, eventService.findSalesEvents(), principal)
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/date/report.*")
    fun generateSalesEventsByDate(
        @RequestParam(name = "date")
        dateString: String,
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSalesEvents(dateString), principal)
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/period/report.*")
    fun generateSalesEventsByDateRange(
        @RequestParam(name = "from") from: String,
        @RequestParam(name = "to") to: String,
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSalesEvents(from, to), principal)
    }

    @GetMapping(SALES_DIARY_DAILY_SALES_EVENTS_REPORT)
    fun generateDailySalesEvents(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findDailySalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS_REPORT)
    fun generateWeeklySalesEvents(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findWeeklySalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS_REPORT)
    fun generateMonthlySalesEvents(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findMonthlySalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS_REPORT)
    fun generateQuarterSalesEvent(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findQuarterlySalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS_REPORT)
    fun generateSemesterSalesEvents(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSemesterSalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS_REPORT)
    fun generateYearlySalesEvents(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findYearlySalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_SALES_EVENTS_EXTENDED_REPORT)
    fun generateExtendedReport(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generatedExtendedSalesEventReport(
            swe, principal,
            extractAdditionalDataFromSalesEvents(
                eventService.findSalesEvents(),
                authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
                    eventService.findSalesEventsMetrics(currentUser)
                })
        )
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/period/extended-report.*")
    fun generateExtendedPeriodicSalesReport(
        @RequestParam(name = "from") from: String,
        @RequestParam(name = "to") to: String,
        principal: Principal, swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generatedExtendedSalesEventReport(
            swe, principal,
            extractAdditionalDataFromSalesEvents(eventService.findSalesEvents(from, to),
                authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
                    eventService.findSalesEventsMetrics(from, to, currentUser)
                })
        )
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/date/extended-report.*")
    fun generateExtendedDatedSalesReport(
        @RequestParam(name = "date") dateString: String,
        principal: Principal, swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generatedExtendedSalesEventReport(
            swe, principal,
            extractAdditionalDataFromSalesEvents(eventService.findSalesEvents(dateString),
                authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
                    eventService.findSalesEventsMetrics(dateString, currentUser)
                })
        )
    }

    @GetMapping(SALES_DIARY_DAILY_SALES_EVENTS_EXTENDED_REPORT)
    fun generateExtendedDailySalesReport(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generatedExtendedSalesEventReport(
            swe, principal,
            extractAdditionalDataFromSalesEvents(eventService.findDailySalesEvents(),
                authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
                    eventService.findDailySalesEventsMetrics(currentUser)
                })
        )
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS_EXTENDED_REPORT)
    fun generateExtendedWeeklySalesReport(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generatedExtendedSalesEventReport(
            swe, principal,
            extractAdditionalDataFromSalesEvents(eventService.findWeeklySalesEvents(),
                authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
                    eventService.findWeeklySalesEventsMetrics(currentUser)
                })
        )
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS_EXTENDED_REPORT)
    fun generateExtendedMonthlySalesReport(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generatedExtendedSalesEventReport(
            swe, principal,
            extractAdditionalDataFromSalesEvents(eventService.findMonthlySalesEvents(),
                authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
                    eventService.findMonthlySalesEventsMetrics(currentUser)
                })
        )
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS_EXTENDED_REPORT)
    fun generateExtendedQuarterlySalesReport(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generatedExtendedSalesEventReport(
            swe, principal,
            extractAdditionalDataFromSalesEvents(eventService.findQuarterlySalesEvents(),
                authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
                    eventService.findQuarterlySalesEventsMetrics(currentUser)
                })
        )
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS_EXTENDED_REPORT)
    fun generateExtendedSemesterSalesReport(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generatedExtendedSalesEventReport(
            swe, principal,
            extractAdditionalDataFromSalesEvents(eventService.findSemesterSalesEvents(),
                authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
                    eventService.findSemesterSalesEventsMetrics(currentUser)
                })
        )
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS_EXTENDED_REPORT)
    fun generateExtendedYearlySalesEvent(
        principal: Principal,
        swe: ServerWebExchange
    ): Mono<ResponseEntity<InputStreamResource>> {
        return generatedExtendedSalesEventReport(
            swe, principal,
            extractAdditionalDataFromSalesEvents(eventService.findYearlySalesEvents(),
                authenticatedUser.getCurrentUser().flatMap { currentUser ->
                    eventService.findYearlySalesEventsMetrics(currentUser)
                })
        )
    }

    private fun generateSalesEventReport(
        swe: ServerWebExchange,
        data: Flux<SalesEvent>,
        principal: Principal
    ): Mono<ResponseEntity<InputStreamResource>> {
        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            reportService.generateReport(swe, filterSalesEventData(currentUser, data))
                .map { ResponseEntity.ok().body(InputStreamResource(it)) }
        }
    }

    private fun extractAdditionalDataFromSalesEvents(
        salesEvents: Flux<SalesEvent>,
        metrics: Mono<SalesMetrics>
    ): Mono<Map<String?, List<Mappable>>> {
        return salesEvents.run {
            val customers: Flux<Customer> = flatMap { event ->
                customerService
                    .findAllCustomers().filter { event.customerId == it.id }
            }
            val soldProducts: Flux<Product> = flatMap { event ->
                productService
                    .findProducts().filter { event.productId == it.id }
            }
            val traderProducts:Flux<Product> = authenticatedUser.getCurrentUser().flatMapMany { currentUser ->
                productService.findProducts().filter {currentUser.id == it.traderId }
            }
            Flux.mergeSequential(
                this, customers, soldProducts,
                flatMap { event -> traderService.findAllTraders().filter { event.traderId == it.id } },
                flatMap { event -> creditService.findAllCredits().filter { event.traderId == it.traderId() } },
                flatMap { event -> expenditureService.findExpenditures().filter { event.traderId == it.traderId } },
                Flux.just(
                    CustomerRetentionMetrics(
                        metrics, customers, customers
                            .count().toFuture().join()
                            .toInt()
                    )
                ),
                flatMap { StockDialImageGenerator.generateDialImage(traderProducts) }.distinct(),
                metrics.flux()
            ).collectList().map { mergedData ->
                mergedData.groupBy { it::class.simpleName }
            }
        }

    }

    private fun generatedExtendedSalesEventReport(
        swe: ServerWebExchange,
        principal: Principal,
        data: Mono<Map<String?, List<Mappable>>>
    ): Mono<ResponseEntity<InputStreamResource>> {
        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            reportService.generateExtendedReport(swe, filterResourceData(currentUser, data))
                .map { ResponseEntity.ok().body(InputStreamResource(it)) }
        }
    }

    private fun filterSalesEventData(user: User, salesEvents: Flux<SalesEvent>): Flux<SalesEvent> {
        return salesEvents
            .filter { ownsThisResource(user, it) }
            .switchIfEmpty(throwAuthenticationException())
    }

    private fun filterResourceData(
        user: User,
        data: Mono<Map<String?, List<Mappable>>>
    ): Mono<Map<String?, List<Mappable>>> {
        return data.apply {
            map {
                it.values.map { values ->
                    values.filter { resource -> ownsThisResource(user, resource) }
                }
            }
        }
    }

    private fun ownsThisResource(currentUser: User, it: SalesEvent) = currentUser.id == it.traderId

    private fun ownsThisResource(currentUser: User, resource: Mappable): Boolean {
        return resource.toMap().run {
            val key = "traderId"
            containsKey(key) && currentUser.trader == get(key)
        }
    }

}

