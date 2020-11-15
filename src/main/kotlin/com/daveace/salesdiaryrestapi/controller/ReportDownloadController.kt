package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SEMESTER_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS_REPORT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_YEARLY_SALES_EVENTS_REPORT
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
import java.security.Principal

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
    fun generateAllSalesEvents(swe: ServerWebExchange, principal: Principal): Mono<ResponseEntity<InputStreamResource>> {
        // return generateTheReport(swe, eventService.findSalesEvents())
        return generateSalesEventReport(swe, eventService.findDailySalesEvents(), principal)
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/date/report.*")
    fun generateSalesEventsByDate(@RequestParam(name = "date")
                                  dateString: String,
                                  principal: Principal,
                                  swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSalesEvents(dateString), principal)
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/period/report.*")
    fun generateSalesEventsByDateRange(
            @RequestParam(name = "from") from: String,
            @RequestParam(name = "to") to: String ,
            principal: Principal,
            swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSalesEvents(from, to), principal)
    }

    @GetMapping(SALES_DIARY_DAILY_SALES_EVENTS_REPORT)
    fun generateDailySalesEvents(principal: Principal, swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findDailySalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS_REPORT)
    fun generateWeeklySalesEvents(principal:Principal, swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findWeeklySalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS_REPORT)
    fun generateMonthlySalesEvents(principal:Principal, swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findMonthlySalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS_REPORT)
    fun generateQuarterSalesEvent(principal:Principal, swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findQuarterlySalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS_REPORT)
    fun generateSemesterSalesEvents(principal: Principal, swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findSemesterSalesEvents(), principal)
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS_REPORT)
    fun generateYearlySalesEvents(principal: Principal, swe: ServerWebExchange): Mono<ResponseEntity<InputStreamResource>> {
        return generateSalesEventReport(swe, eventService.findYearlySalesEvents(), principal)
    }

    private fun generateSalesEventReport(swe: ServerWebExchange, data: Flux<SalesEvent>, principal: Principal): Mono<ResponseEntity<InputStreamResource>> {
        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            reportService.generateReport(swe, filterSalesEventData(currentUser, data))
                    .map { ResponseEntity.ok().body(InputStreamResource(it)) }
        }
    }

    private fun filterSalesEventData(user: User, salesEvents: Flux<SalesEvent>): Flux<SalesEvent> {
        return salesEvents
                .filter { ownsThisResource(user, it) }
                .switchIfEmpty(throwAuthenticationException())
    }

    private fun ownsThisResource(currentUser: User, it: SalesEvent) = currentUser.id == it.traderId

}

