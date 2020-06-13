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

    @Autowired
    private lateinit var service: ReactiveSalesEventService

    companion object {
        val TODAY: LocalDate = LocalDate.now()
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

    @GetMapping("$SALES_DIARY_SALES_EVENTS/{date}")
    fun findAllSalesEventsByDate(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String,
            @PathVariable date: LocalDate
    ): Flux<PagedModel<SalesEventModel>> {
        return linkTo(methodOn(this::class.java).findAllSalesEventsByDate(
                size, page, by, dir, date
        )).withSelfRel().toMono().flatMapMany { link ->
            paginator.paginate(SalesEventModelAssembler(),
                    service.findSalesEvents(date),
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
            @RequestParam(name = "from") from: LocalDate = TODAY,
            @RequestParam(name = "to") to: LocalDate = TODAY
    ): Flux<PagedModel<SalesEventModel>> {
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
    fun findDailySalesEventsMetrics(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ):Flux<PagedModel<*>>{
        TODO("Not yet implemented")
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS)
    fun findWeeklySalesEventsMetrics(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ):Flux<PagedModel<*>>{
        TODO("Not yet implemented")
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS_METRICS)
    fun findMonthlySalesEventsMetrics(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ):Flux<PagedModel<*>>{
        TODO("Not yet implemented")
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS_METRICS)
    fun findQuarterlySalesEventsMetrics(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ):Flux<PagedModel<*>>{
        TODO("Not yet Implemented")
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS_METRICS)
    fun findSemesterSalesEventsMetrics(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ):Flux<PagedModel<*>>{
        TODO("Not yet implemented")
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS_METRICS)
    fun findYearlySalesEventsMetrics(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ):Flux<PagedModel<*>>{
        TODO("Not yet implemented")
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS_METRICS/{date}")
    fun findSalesEventsMetricsByDate(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String,
            @PathVariable date:LocalDate
    ):Flux<PagedModel<*>>{
        TODO("Not yet implemented")
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS_METRICS/period")
    fun findSalesEventsMetricsByDateRange(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String,
            @RequestParam(name = "from") from:LocalDate = LocalDate.now(),
            @RequestParam(name = "to") to:LocalDate = LocalDate.now()
    ):Flux<PagedModel<*>>{
        TODO("Not yet implemented")
    }
}

