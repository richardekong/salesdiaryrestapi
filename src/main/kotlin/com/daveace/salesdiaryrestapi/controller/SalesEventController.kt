package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_DAILY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_MONTHLY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_QUARTERLY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENT
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SEMESTER_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_SEMESTER_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_YEARLY_SALES_EVENTS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_YEARLY_SALES_EVENTS_METRICS
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.hateoas.assembler.SalesEventModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.model.SalesEventModel
import com.daveace.salesdiaryrestapi.hateoas.model.SalesMetricsModel
import com.daveace.salesdiaryrestapi.service.ReactiveSalesEventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import javax.validation.Valid

@RestController
@RequestMapping(API)
class SalesEventController : BaseController() {

    private lateinit var service: ReactiveSalesEventService

    companion object {
        val TODAY: LocalDate = LocalDate.now()
    }

    @Autowired
    fun initService(service: ReactiveSalesEventService) {
        this.service = service
    }

//    @Autowired
//    fun initReportService(reportService: ReactiveSalesReportService) {
//        this.reportService = reportService
//    }

    @PostMapping(SALES_DIARY_SALES_EVENTS)
    @ResponseStatus(code = HttpStatus.CREATED)
    fun saveSalesEvent(@RequestBody @Valid event: SalesEvent): Mono<SalesEventModel> {
        return authenticatedUser
                .getCurrentUser()
                .filter { ownsThisEvent(it, event) }
                .switchIfEmpty(throwAuthenticationException())
                .flatMap {
                    service.saveSalesEvent(event)
                            .flatMap {
                                respondWithReactiveLink(
                                        SalesEventModel(it),
                                        methodOn(this::class.java)
                                                .saveSalesEvent(event))
                            }
                }

    }

    @GetMapping("$SALES_DIARY_SALES_EVENT{id}")
    fun findSalesEventById(@PathVariable id: String): Mono<SalesEventModel> {
        return authenticatedUser
                .getCurrentUser()
                .flatMap { currentUser ->
                    service.findSalesEvent(id)
                            .filter { ownsThisEvent(currentUser, it) }
                            .switchIfEmpty(throwAuthenticationException())
                            .flatMap {
                                respondWithReactiveLink(
                                        SalesEventModel(it), methodOn(
                                        this.javaClass).findSalesEventById(id)
                                )
                            }
                }
    }

    @GetMapping(SALES_DIARY_SALES_EVENTS)
    fun findAllSalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return authenticatedUser
                .getCurrentUser()
                .flatMapMany { currentUser ->
                    linkTo(methodOn(this.javaClass).findAllSalesEvents(size, page, by, dir))
                            .withSelfRel()
                            .toMono()
                            .flatMapMany { link ->
                                paginator.paginate(SalesEventModelAssembler(),
                                        service.findSalesEvents().filter { ownsThisEvent(currentUser, it) }
                                                .switchIfEmpty(throwAuthenticationException()),
                                        PageRequest.of(page, size),
                                        link, configureSortProperties(by, dir))
                            }
                }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/dates")
    fun findAllSalesEventsByDate(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String,
            @RequestParam(name = "date")
            dateString: String = LocalDate.now().toString()): Flux<PagedModel<SalesEventModel>> {
        return authenticatedUser.getCurrentUser()
                .flatMapMany { currentUser ->
                    linkTo(methodOn(this.javaClass)
                            .findAllSalesEventsByDate(size, page, by, dir, dateString))
                            .withSelfRel()
                            .toMono()
                            .flatMapMany { link ->
                                paginator.paginate(SalesEventModelAssembler(),
                                        service.findSalesEvents(dateString)
                                                .filter { ownsThisEvent(currentUser, it) }
                                                .switchIfEmpty(throwAuthenticationException()),
                                        PageRequest.of(page, size),
                                        link, configureSortProperties(by, dir))
                            }
                }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/period")
    fun findAllSalesEventsByDateRange(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String,
            @RequestParam(name = "from") from: String = TODAY.toString(),
            @RequestParam(name = "to") to: String = TODAY.toString()): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser
                .getCurrentUser()
                .flatMapMany { currentUser ->
                    linkTo(methodOn(this.javaClass).findAllSalesEventsByDateRange(size, page, by, dir, from, to))
                            .withSelfRel()
                            .toMono()
                            .flatMapMany { link ->
                                paginator.paginate(SalesEventModelAssembler(),
                                        service
                                                .findSalesEvents(from, to)
                                                .filter { ownsThisEvent(currentUser, it) }
                                                .switchIfEmpty(throwAuthenticationException()),
                                        PageRequest.of(page, size), link, configureSortProperties(by, dir))
                            }

                }
    }

    @GetMapping(SALES_DIARY_DAILY_SALES_EVENTS)
    fun findDailySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return authenticatedUser
                .getCurrentUser()
                .flatMapMany { currentUser ->
                    linkTo(methodOn(this.javaClass).findDailySalesEvents(size, page, by, dir))
                            .withSelfRel()
                            .toMono()
                            .flatMapMany { link ->
                                paginator.paginate(SalesEventModelAssembler(),
                                        service.findDailySalesEvents()
                                                .filter { ownsThisEvent(currentUser, it) }
                                                .switchIfEmpty(throwAuthenticationException()),
                                        PageRequest.of(page, size), link, configureSortProperties(by, dir))
                            }
                }
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS)
    fun findWeeklySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser
                .getCurrentUser()
                .flatMapMany { currentUser ->
                    linkTo(methodOn(this.javaClass).findWeeklySalesEvents(size, page, by, dir))
                            .withSelfRel()
                            .toMono()
                            .flatMapMany { link ->
                                paginator.paginate(SalesEventModelAssembler(),
                                        service.findWeeklySalesEvents().filter { ownsThisEvent(currentUser, it) }
                                                .switchIfEmpty(throwAuthenticationException()),
                                        PageRequest.of(page, size), link, configureSortProperties(by, dir))
                            }
                }
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS)
    fun findMonthlySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser.getCurrentUser().flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findMonthlySalesEvents(size, page, by, dir))
                    .withSelfRel().toMono().flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(),
                                service.findMonthlySalesEvents()
                                        .filter { ownsThisEvent(currentUser, it) }
                                        .switchIfEmpty(throwAuthenticationException()),
                                PageRequest.of(page, size),
                                link, configureSortProperties(by, dir))
                    }
        }
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS)
    fun findQuarterlySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser.getCurrentUser().flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findQuarterlySalesEvents(size, page, by, dir))
                    .withSelfRel().toMono().flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(),
                                service.findQuarterlySalesEvents()
                                        .filter { ownsThisEvent(currentUser, it) }
                                        .switchIfEmpty(throwAuthenticationException()),
                                PageRequest.of(page, size), link, configureSortProperties(by, dir))
                    }
        }
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS)
    fun findSemesterSalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return authenticatedUser.getCurrentUser().flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findSemesterSalesEvents(size, page, by, dir))
                    .withSelfRel().toMono().flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(), service.findSemesterSalesEvents().filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()), PageRequest.of(page, size), link, configureSortProperties(by, dir))
                    }
        }
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS)
    fun findYearlySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser.getCurrentUser().flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findYearlySalesEvents(size, page, by, dir))
                    .withSelfRel().toMono().flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(), service.findYearlySalesEvents().filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()), PageRequest.of(page, size), link, configureSortProperties(by, dir))
                    }
        }
    }

    @GetMapping(SALES_DIARY_DAILY_SALES_EVENTS_METRICS)
    fun findDailySalesEventsMetrics(): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            service.findDailySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(SalesMetricsModel(it),
                        methodOn(this.javaClass).findDailySalesEventsMetrics())
            }
        }
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS)
    fun findWeeklySalesEventsMetrics(): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            service.findWeeklySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(SalesMetricsModel(it),
                        methodOn(this.javaClass).findDailySalesEventsMetrics())
            }
        }
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS_METRICS)
    fun findMonthlySalesEventsMetrics(): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            service.findMonthlySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(SalesMetricsModel(it),
                        methodOn(this.javaClass).findMonthlySalesEventsMetrics())
            }
        }
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS_METRICS)
    fun findQuarterlySalesEventsMetrics(): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            service.findQuarterlySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(SalesMetricsModel(it),
                        methodOn(this.javaClass).findQuarterlySalesEventsMetrics())
            }
        }
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS_METRICS)
    fun findSemesterSalesEventsMetrics(): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            service.findSemesterSalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(SalesMetricsModel(it),
                        methodOn(this.javaClass).findSemesterSalesEventsMetrics())
            }
        }
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS_METRICS)
    fun findYearlySalesEventsMetrics(): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            service.findYearlySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(SalesMetricsModel(it),
                        methodOn(this.javaClass).findYearlySalesEventsMetrics())
            }
        }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS_METRICS/{dateString}")
    fun findSalesEventsMetricsByDate(@PathVariable dateString: String): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            service.findSalesEventsMetrics(dateString, currentUser).flatMap {
                respondWithReactiveLink(SalesMetricsModel(it),
                        methodOn(this.javaClass).findSalesEventsMetricsByDate(dateString))
            }
        }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS_METRICS/period")
    fun findSalesEventsMetricsByDateRange(
            @RequestParam(name = "from") from: String = TODAY.toString(),
            @RequestParam(name = "to") to: String = TODAY.toString()): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            service.findSalesEventsMetrics(from, to, currentUser).flatMap {
                respondWithReactiveLink(SalesMetricsModel(it),
                        methodOn(this.javaClass).findSalesEventsMetricsByDateRange(to, from))
            }
        }
    }

    private fun ownsThisEvent(currentUser: User, event: SalesEvent) = currentUser.id == event.traderId


}

