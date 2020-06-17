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
import com.daveace.salesdiaryrestapi.hateoas.assembler.SalesEventModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.model.SalesEventModel
import com.daveace.salesdiaryrestapi.hateoas.model.SalesMetricsModel
import com.daveace.salesdiaryrestapi.service.ReactiveSalesEventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
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

    @Autowired
    private lateinit var service: ReactiveSalesEventService

    companion object {
        val TODAY: LocalDate = LocalDate.now()
        private const val DATE_PATTERN = "yyyyMMdd"
    }

    @PostMapping(SALES_DIARY_SALES_EVENTS)
    @ResponseStatus(code = HttpStatus.CREATED)
    fun saveSalesEvent(@RequestBody @Valid event: SalesEvent): Mono<SalesEventModel> {
        return service.saveSalesEvent(event).flatMap {
            respondWithReactiveLink(SalesEventModel(it), methodOn(this::class.java).saveSalesEvent(event))
        }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENT{id}")
    fun findSalesEventById(@PathVariable id: String): Mono<SalesEventModel> {
        return service.findSalesEvent(id).flatMap {
            respondWithReactiveLink(SalesEventModel(it), methodOn(this::class.java).findSalesEventById(id))
        }
    }

    @GetMapping(SALES_DIARY_SALES_EVENTS)
    fun findAllSalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return linkTo(methodOn(this::class.java).findAllSalesEvents(
                size, page, by, dir
        )).withSelfRel().toMono().flatMapMany { link ->
            paginator.paginate(SalesEventModelAssembler(),
                    service.findSalesEvents(),
                    PageRequest.of(page, size),
                    link, configureSortProperties(by, dir))
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
        return linkTo(methodOn(this::class.java).findAllSalesEventsByDate(
                size, page, by, dir, dateString
        )).withSelfRel().toMono().flatMapMany { link ->
            paginator.paginate(SalesEventModelAssembler(),
                    service.findSalesEvents(dateString),
                    PageRequest.of(page, size),
                    link, configureSortProperties(by, dir))
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

        return linkTo(methodOn(this::class.java).findAllSalesEventsByDateRange(size, page, by, dir, from, to))
                .withSelfRel().toMono().flatMapMany { link ->
                    paginator.paginate(SalesEventModelAssembler(), service.findSalesEvents(from, to),
                            PageRequest.of(page, size), link, configureSortProperties(by, dir))
                }
    }

    @GetMapping(SALES_DIARY_DAILY_SALES_EVENTS)
    fun findDailySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return linkTo(methodOn(this::class.java).findDailySalesEvents(
                size, page, by, dir
        )).withSelfRel().toMono().flatMapMany { link ->
            paginator.paginate(SalesEventModelAssembler(), service.findDailySalesEvents(),
                    PageRequest.of(page, size), link, configureSortProperties(by, dir))
        }
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS)
    fun findWeeklySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return linkTo(methodOn(this::class.java).findWeeklySalesEvents(
                size, page, by, dir
        )).withSelfRel().toMono().flatMapMany { link ->
            paginator.paginate(SalesEventModelAssembler(), service.findWeeklySalesEvents(),
                    PageRequest.of(page, size), link, configureSortProperties(by, dir))
        }
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS)
    fun findMonthlySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return linkTo(methodOn(this::class.java).findMonthlySalesEvents(
                size, page, by, dir
        )).withSelfRel().toMono().flatMapMany { link ->
            paginator.paginate(SalesEventModelAssembler(), service.findMonthlySalesEvents(),
                    PageRequest.of(page, size), link, configureSortProperties(by, dir))
        }
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS)
    fun findQuarterlySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return linkTo(methodOn(this::class.java).findQuarterlySalesEvents(size, page, by, dir))
                .withSelfRel().toMono().flatMapMany { link ->
                    paginator.paginate(SalesEventModelAssembler(), service.findQuarterlySalesEvents(),
                            PageRequest.of(page, size), link, configureSortProperties(by, dir))
                }
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS)
    fun findSemesterSalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return linkTo(methodOn(this::class.java).findSemesterSalesEvents(size, page, by, dir))
                .withSelfRel().toMono().flatMapMany { link ->
                    paginator.paginate(SalesEventModelAssembler(), service.findSemesterSalesEvents(),
                            PageRequest.of(page, size), link, configureSortProperties(by, dir))
                }
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS)
    fun findYearlySalesEvents(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<SalesEventModel>> {
        return linkTo(methodOn(this::class.java).findYearlySalesEvents(size, page, by, dir))
                .withSelfRel().toMono().flatMapMany { link ->
                    paginator.paginate(SalesEventModelAssembler(), service.findYearlySalesEvents(),
                            PageRequest.of(page, size), link, configureSortProperties(by, dir))
                }
    }

    @GetMapping(SALES_DIARY_DAILY_SALES_EVENTS_METRICS)
    fun findDailySalesEventsMetrics(): Mono<SalesMetricsModel> {
        return service.findDailySalesEventsMetrics().flatMap {
            respondWithReactiveLink(SalesMetricsModel(it), methodOn(this::class.java).findDailySalesEventsMetrics())
        }
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS)
    fun findWeeklySalesEventsMetrics(): Mono<SalesMetricsModel> {
        return service.findWeeklySalesEventsMetrics().flatMap {
            respondWithReactiveLink(SalesMetricsModel(it), methodOn(this::class.java).findWeeklySalesEventsMetrics())
        }
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS_METRICS)
    fun findMonthlySalesEventsMetrics(): Mono<SalesMetricsModel> {
        return service.findMonthlySalesEventsMetrics().flatMap {
            respondWithReactiveLink(SalesMetricsModel(it), methodOn(this::class.java).findMonthlySalesEventsMetrics())
        }
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS_METRICS)
    fun findQuarterlySalesEventsMetrics(): Mono<SalesMetricsModel> {
        return service.findQuarterlySalesEventsMetrics().flatMap {
            respondWithReactiveLink(SalesMetricsModel(it), methodOn(this::class.java).findQuarterlySalesEventsMetrics())
        }
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS_METRICS)
    fun findSemesterSalesEventsMetrics(): Mono<SalesMetricsModel> {
        return service.findSemesterSalesEventsMetrics().flatMap {
            respondWithReactiveLink(SalesMetricsModel(it), methodOn(this::class.java).findSemesterSalesEventsMetrics())
        }
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS_METRICS)
    fun findYearlySalesEventsMetrics(): Mono<SalesMetricsModel> {
        return service.findYearlySalesEventsMetrics().flatMap {
            respondWithReactiveLink(SalesMetricsModel(it), methodOn(this::class.java).findYearlySalesEventsMetrics())
        }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS_METRICS/{dateString}")
    fun findSalesEventsMetricsByDate(@PathVariable dateString: String): Mono<SalesMetricsModel> {
        return service.findSalesEventsMetrics(dateString).flatMap {
            respondWithReactiveLink(SalesMetricsModel(it), methodOn(this::class.java).findSalesEventsMetricsByDate(dateString))
        }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS_METRICS/period")
    fun findSalesEventsMetricsByDateRange(
            @RequestParam(name = "from") from: String = TODAY.toString(),
            @RequestParam(name = "to") to: String = TODAY.toString()): Mono<SalesMetricsModel> {

        return service.findSalesEventsMetrics(from, to).flatMap {
            respondWithReactiveLink(SalesMetricsModel(it), methodOn(this::class.java).findSalesEventsMetricsByDateRange(from, to))
        }
    }
}

