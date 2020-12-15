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
import com.daveace.salesdiaryrestapi.hateoas.assembler.SalesEventModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.model.SalesEventModel
import com.daveace.salesdiaryrestapi.hateoas.model.SalesMetricsModel
import com.daveace.salesdiaryrestapi.service.ReactiveSalesEventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal
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

    @PostMapping(SALES_DIARY_SALES_EVENTS)
    @ResponseStatus(code = HttpStatus.CREATED)
    fun saveSalesEvent(@RequestBody @Valid event: SalesEvent, principal: Principal): Mono<SalesEventModel> {
        return authenticatedUser
            .getCurrentUser(principal)
            .filter { ownsThisEvent(it, event) }
            .switchIfEmpty(throwAuthenticationException())
            .flatMap {
                service.saveSalesEvent(event)
                    .flatMap {
                        respondWithReactiveLink(
                            SalesEventModel(it),
                            methodOn(this::class.java)
                                .saveSalesEvent(event, principal)
                        )
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
                                this.javaClass
                            ).findSalesEventById(id)
                        )
                    }
            }
    }

    @GetMapping(SALES_DIARY_SALES_EVENTS)
    fun findAllSalesEvents(
        @RequestParam(required = false) params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<SalesEventModel>> {
        return authenticatedUser
            .getCurrentUser(principal)
            .flatMapMany { currentUser ->
                linkTo(methodOn(this.javaClass).findAllSalesEvents(params, principal))
                    .withSelfRel()
                    .toMono()
                    .flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(),
                            service.findSalesEvents().filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()),
                            specifyPageRequest(params),
                            link, configureSortProperties(params))
                    }
            }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/dates")
    fun findAllSalesEventsByDate(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<SalesEventModel>> {
        return authenticatedUser.getCurrentUser(principal)
            .flatMapMany { currentUser ->
                linkTo(
                    methodOn(this.javaClass)
                        .findAllSalesEventsByDate(params, principal)
                )
                    .withSelfRel()
                    .toMono()
                    .flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(),
                            service.findSalesEvents(params.getOrDefault("date", LocalDate.now().toString()))
                                .filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()),
                            specifyPageRequest(params),
                            link,
                            configureSortProperties(params))
                    }
            }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS/period")
    fun findAllSalesEventsByDateRange(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser
            .getCurrentUser(principal)
            .flatMapMany { currentUser ->
                linkTo(methodOn(this.javaClass).findAllSalesEventsByDateRange(params, principal))
                    .withSelfRel()
                    .toMono()
                    .flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(),
                            service
                                .findSalesEvents(
                                    params.getOrDefault("from", TODAY.toString()),
                                    params.getOrDefault("to", TODAY.toString())
                                )
                                .filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()),
                            specifyPageRequest(params),
                            link,
                            configureSortProperties(params))
                    }

            }
    }

    @GetMapping(SALES_DIARY_DAILY_SALES_EVENTS)
    fun findDailySalesEvents(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<SalesEventModel>> {
        return authenticatedUser
            .getCurrentUser(principal)
            .flatMapMany { currentUser ->
                linkTo(methodOn(this.javaClass).findDailySalesEvents(params, principal))
                    .withSelfRel()
                    .toMono()
                    .flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(),
                            service.findDailySalesEvents()
                                .filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()),
                            specifyPageRequest(params), link, configureSortProperties(params))
                    }
            }
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS)
    fun findWeeklySalesEvents(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser
            .getCurrentUser(principal)
            .flatMapMany { currentUser ->
                linkTo(methodOn(this.javaClass).findWeeklySalesEvents(params, principal))
                    .withSelfRel()
                    .toMono()
                    .flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(),
                            service.findWeeklySalesEvents().filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()),
                            specifyPageRequest(params), link, configureSortProperties(params))
                    }
            }
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS)
    fun findMonthlySalesEvents(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser.getCurrentUser(principal)
            .flatMapMany { currentUser ->
                linkTo(methodOn(this.javaClass).findMonthlySalesEvents(params, principal))
                    .withSelfRel().toMono().flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(),
                            service.findMonthlySalesEvents()
                                .filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()),
                            specifyPageRequest(params), link, configureSortProperties(params))
                    }
            }
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS)
    fun findQuarterlySalesEvents(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser.getCurrentUser(principal)
            .flatMapMany { currentUser ->
                linkTo(methodOn(this.javaClass).findQuarterlySalesEvents(params, principal))
                    .withSelfRel().toMono().flatMapMany { link ->
                        paginator.paginate(SalesEventModelAssembler(),
                            service.findQuarterlySalesEvents()
                                .filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()),
                            specifyPageRequest(params), link, configureSortProperties(params))
                    }
            }
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS)
    fun findSemesterSalesEvents(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<SalesEventModel>> {
        return authenticatedUser.getCurrentUser(principal)
            .flatMapMany { currentUser ->
                linkTo(methodOn(this.javaClass).findSemesterSalesEvents(params, principal))
                    .withSelfRel().toMono().flatMapMany { link ->
                        paginator.paginate(
                            SalesEventModelAssembler(),
                            service.findSemesterSalesEvents().filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()),
                            specifyPageRequest(params), link, configureSortProperties(params))
                    }
            }
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS)
    fun findYearlySalesEvents(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<SalesEventModel>> {

        return authenticatedUser.getCurrentUser(principal)
            .flatMapMany { currentUser ->
                linkTo(methodOn(this.javaClass).findYearlySalesEvents(params, principal))
                    .withSelfRel().toMono().flatMapMany { link ->
                        paginator.paginate(
                            SalesEventModelAssembler(),
                            service.findYearlySalesEvents().filter { ownsThisEvent(currentUser, it) }
                                .switchIfEmpty(throwAuthenticationException()),
                            specifyPageRequest(params), link, configureSortProperties(params))
                    }
            }
    }

    @GetMapping(SALES_DIARY_DAILY_SALES_EVENTS_METRICS)
    fun findDailySalesEventsMetrics(principal: Principal): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            service.findDailySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(
                    SalesMetricsModel(it),
                    methodOn(this.javaClass).findDailySalesEventsMetrics(principal)
                )
            }
        }
    }

    @GetMapping(SALES_DIARY_WEEKLY_SALES_EVENTS_METRICS)
    fun findWeeklySalesEventsMetrics(principal: Principal): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            service.findWeeklySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(
                    SalesMetricsModel(it),
                    methodOn(this.javaClass).findDailySalesEventsMetrics(principal)
                )
            }
        }
    }

    @GetMapping(SALES_DIARY_MONTHLY_SALES_EVENTS_METRICS)
    fun findMonthlySalesEventsMetrics(principal: Principal): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            service.findMonthlySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(
                    SalesMetricsModel(it),
                    methodOn(this.javaClass).findMonthlySalesEventsMetrics(principal)
                )
            }
        }
    }

    @GetMapping(SALES_DIARY_QUARTERLY_SALES_EVENTS_METRICS)
    fun findQuarterlySalesEventsMetrics(principal: Principal): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            service.findQuarterlySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(
                    SalesMetricsModel(it),
                    methodOn(this.javaClass).findQuarterlySalesEventsMetrics(principal)
                )
            }
        }
    }

    @GetMapping(SALES_DIARY_SEMESTER_SALES_EVENTS_METRICS)
    fun findSemesterSalesEventsMetrics(principal: Principal): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            service.findSemesterSalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(
                    SalesMetricsModel(it),
                    methodOn(this.javaClass).findSemesterSalesEventsMetrics(principal)
                )
            }
        }
    }

    @GetMapping(SALES_DIARY_YEARLY_SALES_EVENTS_METRICS)
    fun findYearlySalesEventsMetrics(principal: Principal): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            service.findYearlySalesEventsMetrics(currentUser).flatMap {
                respondWithReactiveLink(
                    SalesMetricsModel(it),
                    methodOn(this.javaClass).findYearlySalesEventsMetrics(principal)
                )
            }
        }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS_METRICS/{dateString}")
    fun findSalesEventsMetricsByDate(@PathVariable dateString: String, principal: Principal): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            service.findSalesEventsMetrics(dateString, currentUser).flatMap {
                respondWithReactiveLink(
                    SalesMetricsModel(it),
                    methodOn(this.javaClass).findSalesEventsMetricsByDate(dateString, principal)
                )
            }
        }
    }

    @GetMapping("$SALES_DIARY_SALES_EVENTS_METRICS/period")
    fun findSalesEventsMetricsByDateRange(
        @RequestParam(name = "from") from: String = TODAY.toString(),
        @RequestParam(name = "to") to: String = TODAY.toString(),
        principal: Principal
    ): Mono<SalesMetricsModel> {

        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            service.findSalesEventsMetrics(from, to, currentUser).flatMap {
                respondWithReactiveLink(
                    SalesMetricsModel(it),
                    methodOn(this.javaClass).findSalesEventsMetricsByDateRange(to, from, principal)
                )
            }
        }
    }

    private fun ownsThisEvent(currentUser: User, event: SalesEvent) = currentUser.id == event.traderId

}

