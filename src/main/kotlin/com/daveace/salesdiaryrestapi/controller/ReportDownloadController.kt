package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.configuration.ReportFilter.Companion.REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.service.ReactiveSalesEventService
import com.daveace.salesdiaryrestapi.service.ReactiveSalesReportService
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
import java.time.LocalDate

@RestController
@RequestMapping(API)
class ReportDownloadController : BaseController() {

    private lateinit var eventService: ReactiveSalesEventService
    private lateinit var reportService: ReactiveSalesReportService

    @Autowired
    fun initEventService(eventService: ReactiveSalesEventService) {
        this.eventService = eventService
    }

    @Autowired
    fun initReportService(reportService: ReactiveSalesReportService) {
        this.reportService = reportService
    }

    @GetMapping(SALES_DIARY_SALES_EVENTS_REPORT)
    fun generateAllSalesEvents(swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSalesEvents())
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/dates$REPORT.*")
    fun generateSalesEventsByDate(@RequestParam(name = "date")
                                  dateString: String = LocalDate.now().toString(),
                                  swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSalesEvents(dateString))
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/period$REPORT.*")
    fun generateSalesEventsByDateRange(
            @RequestParam(name = "from") from: String = LocalDate.now().toString(),
            @RequestParam(name = "to") to: String = LocalDate.now().toString(),
            swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSalesEvents(from, to))
    }

    @GetMapping("$SALES_DIARY_DAILY_SALES_EVENTS$REPORT.*")
    fun generateDailySalesEvents(swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findDailySalesEvents())
    }

    @GetMapping("$SALES_DIARY_WEEKLY_SALES_EVENTS$REPORT.*")
    fun generateWeeklySalesEvents(swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findWeeklySalesEvents())
    }

    @GetMapping("$SALES_DIARY_MONTHLY_SALES_EVENTS$REPORT.*")
    fun generateMonthlySalesEvents(swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findMonthlySalesEvents())
    }

    @GetMapping("$SALES_DIARY_QUARTERLY_SALES_EVENTS$REPORT.*")
    fun generateQuarterSalesEvent(swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findQuarterlySalesEvents())
    }

    @GetMapping("${ControllerPath.SALES_DIARY_SEMESTER_SALES_EVENTS}$REPORT.*")
    fun generateSemesterSalesEvents(swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSemesterSalesEvents())
    }

    @GetMapping("${ControllerPath.SALES_DIARY_YEARLY_SALES_EVENTS}$REPORT.*")
    fun generateYearlySalesEvents(swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findYearlySalesEvents())
    }

    private fun generateSalesEventReport(swe: ServerWebExchange, data: Flux<SalesEvent>): Mono<ResponseEntity<InputStreamResource>> {
        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            reportService.generateReport(swe, filterSalesEventData(currentUser, data))
                    .map { ResponseEntity.ok().body(InputStreamResource(it)) }
        }
    }

    private fun <T : Any> generateTheReport(swe: ServerWebExchange, data: Flux<T>): Mono<ResponseEntity<InputStreamResource>> {
        return reportService.generateReport(swe, data)
                .map { ResponseEntity.ok().body(InputStreamResource(it)) }
    }

    private fun filterSalesEventData(user: User, salesEvents: Flux<SalesEvent>): Flux<SalesEvent> {
        return salesEvents
                .filter { ownsThisResource(user, it) }
                .switchIfEmpty(throwAuthenticationException())
    }

    private fun ownsThisResource(currentUser: User, it: SalesEvent) = currentUser.id == it.traderId
}
