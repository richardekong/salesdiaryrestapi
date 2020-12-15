package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.domain.Credit
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.exceptionhandling.NotFoundException
import com.daveace.salesdiaryrestapi.hateoas.assembler.CreditAssembler
import com.daveace.salesdiaryrestapi.hateoas.model.CreditModel
import com.daveace.salesdiaryrestapi.service.ReactiveCreditService
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

    @Autowired
    fun initCreditService(creditService: ReactiveCreditService) {
        this.creditService = creditService
    }

    @PostMapping("/sales-diary/credits")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCreditRecord(@Valid @RequestBody credit: Credit): Mono<CreditModel> {
        return creditService.createCreditRecord(credit).flatMap {
            respondWithReactiveLink(CreditModel(it), methodOn(this.javaClass).createCreditRecord(credit))
        }
    }

    @PostMapping("/sales-diary/credit-from-events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun createCreditRecordFromEvent(@Valid @RequestBody event: SalesEvent): Mono<CreditModel> {
        return creditService.createCreditRecord(event).flatMap { credit ->
            respondWithReactiveLink(CreditModel(credit), methodOn(this.javaClass).createCreditRecordFromEvent(event))
        }
    }

    @GetMapping("/sales-diary/credits/{id}")
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

    @GetMapping("/sales-diary/credits/{cId}")
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
                        CreditAssembler(),
                        creditService.findCreditsByCustomerId(cId)
                            .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                            .filter { currentUser.id == it.traderId() }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        specifyPageRequest(params), link, configureSortProperties(params)
                    )
                }
        }
    }

    @GetMapping("sales-diary/credits/{pId}")
    fun findCreditsByProductId(
        @PathVariable pId: String,
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<CreditModel>> {

        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findCreditsByProductId(pId, params, principal)).withSelfRel()
                .toMono().flatMapMany { link ->
                    paginator.paginate(
                        CreditAssembler(),
                        creditService.findCreditsByProductId(pId)
                            .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                            .filter { currentUser.id == it.traderId() }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        specifyPageRequest(params), link, configureSortProperties(params)
                    )
                }
        }
    }

    @GetMapping("/sales-diary/credits")
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
                        CreditAssembler(),
                        creditService.findAllCredits()
                            .filter { currentUser.id == it.traderId() }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        specifyPageRequest(params), link, configureSortProperties(params)
                    )
                }
        }
    }

    @PatchMapping("sales-diary/credits/{id}")
    fun redeemCredit(@PathVariable id: String, principal: Principal): Mono<CreditModel> {
        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            creditService.findCreditById(id)
                .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                .filter { currentUser.id == it.traderId() }
                .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() })
                .flatMap { creditService.redeemCredit(it) }
                .flatMap {
                    respondWithReactiveLink(
                        CreditModel(it),
                        linkTo(methodOn(this.javaClass).redeemCredit(id, principal))
                    )
                }
        }
    }

}

