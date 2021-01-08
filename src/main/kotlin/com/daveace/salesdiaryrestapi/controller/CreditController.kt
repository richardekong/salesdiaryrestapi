package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CREDITS
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CREDITS_FROM_EVENT
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.exceptionhandling.NotFoundException
import com.daveace.salesdiaryrestapi.hateoas.assembler.CreditModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.model.CreditModel
import com.daveace.salesdiaryrestapi.service.ReactiveCreditService
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
import javax.validation.Valid

@RestController
@RequestMapping(API)
class CreditController : BaseController() {


    private lateinit var creditService: ReactiveCreditService
    private lateinit var eventService: ReactiveSalesEventService

    @Autowired
    fun initCreditService(creditService: ReactiveCreditService) {
        this.creditService = creditService
    }

    @Autowired
    fun initEventService(eventService: ReactiveSalesEventService) {
        this.eventService = eventService
    }

    @PostMapping(SALES_DIARY_CREDITS)
    @ResponseStatus(HttpStatus.CREATED)
    fun createCreditRecord(@Valid @RequestBody salesEvent: SalesEvent): Mono<CreditModel> {
        return creditService.createCreditRecord(salesEvent).flatMap {
            respondWithReactiveLink(CreditModel(it), methodOn(this.javaClass).createCreditRecord(salesEvent))
        }
    }

    @PostMapping(SALES_DIARY_CREDITS_FROM_EVENT)
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun createCreditRecordFromEvent(@Valid @RequestBody event: SalesEvent): Mono<CreditModel> {
        return creditService.createCreditRecord(event).flatMap { credit ->
            respondWithReactiveLink(CreditModel(credit), methodOn(this.javaClass).createCreditRecordFromEvent(event))
        }
    }

    @GetMapping("$SALES_DIARY_CREDITS/{id}")
    fun findCredit(@PathVariable id: String): Mono<CreditModel> {
        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            creditService.findCreditById(id)
                .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                .filter { currentUser.id == it.traderId() }
                .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() })
                .filter { it.redeemed().not() }
                .switchIfEmpty(Mono.fromRunnable { throw RuntimeException("Credit's been redeemed!") })
                .flatMap {
                    respondWithReactiveLink(CreditModel(it), methodOn(this.javaClass).findCredit(id))
                }
        }
    }

    @GetMapping("$SALES_DIARY_CREDITS/{cId}")
    fun findCreditsByCustomerId(
        @PathVariable cId: String,
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<CreditModel>> {
        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findCreditsByCustomerId(cId, params, principal))
                .withSelfRel()
                .toMono()
                .flatMapMany { link ->
                    paginator.paginate(
                        CreditModelAssembler(),
                        creditService.findCreditsByCustomerId(cId)
                            .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                            .filter { currentUser.id == it.traderId() }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        specifyPageRequest(params), link, configureSortProperties(params)
                    )
                }
        }
    }

    @GetMapping("$SALES_DIARY_CREDITS/{pId}")
    fun findCreditsByProductId(
        @PathVariable pId: String,
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<CreditModel>> {

        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findCreditsByProductId(pId, params, principal)).withSelfRel()
                .toMono().flatMapMany { link ->
                    paginator.paginate(
                        CreditModelAssembler(),
                        creditService.findCreditsByProductId(pId)
                            .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                            .filter { currentUser.id == it.traderId() }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        specifyPageRequest(params), link, configureSortProperties(params)
                    )
                }
        }
    }

    @GetMapping(SALES_DIARY_CREDITS)
    fun findCredits(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<CreditModel>> {
        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findCredits(params, principal))
                .withSelfRel()
                .toMono()
                .flatMapMany { link ->
                    paginator.paginate(
                        CreditModelAssembler(),
                        creditService.findAllCredits()
                            .filter { currentUser.id == it.traderId() }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        specifyPageRequest(params), link, configureSortProperties(params)
                    )
                }
        }
    }

    @PatchMapping("$SALES_DIARY_CREDITS/{id}")
    fun redeemCredit(@PathVariable id: String, principal: Principal): Mono<CreditModel> {
        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            creditService.findCreditById(id)
                .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                .filter { currentUser.id == it.traderId() }
                .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() })
                .flatMap { creditService.redeemCredit(it) }
                .doOnSuccess {
                    eventService.apply {
                        saveSalesEvent(
                            SalesEvent(
                                it.traderId(),
                                it.productId(),
                                it.customerId(),
                                it.product(),
                                it.quantity(),
                                it.salesPrice().times(-1),
                                it.costPrice(),
                                it.left(),
                                it.location()
                            )
                        )
                    }
                }
                .flatMap {
                    respondWithReactiveLink(
                        CreditModel(it),
                        linkTo(methodOn(this.javaClass).redeemCredit(id, principal))
                    )
                }
        }
    }

}

