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
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.RememberMeAuthenticationToken
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
        @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
        @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
        @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String,
        @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
        principal: Principal
    ): Flux<PagedModel<CreditModel>> {
        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findCreditsByCustomerId(cId, size, page, dir, by, principal))
                .withSelfRel()
                .toMono()
                .flatMapMany { link ->
                    paginator.paginate(
                        CreditAssembler(),
                        creditService.findCreditsByCustomerId(cId)
                            .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                            .filter { currentUser.id == it.traderId() }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        PageRequest.of(page, size), link, configureSortProperties(by, dir)
                    )
                }
        }
    }

    @GetMapping("sales-diary/credits/{pId}")
    fun findCreditsByProductId(
        @PathVariable pId: String,
        @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
        @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
        @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String,
        @RequestParam(name = "by", defaultValue = DEFAULT_SORT_FIELD) by: String,
        principal: Principal
    ): Flux<PagedModel<CreditModel>> {

        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findCreditsByProductId(pId, size, page, dir, by, principal)).withSelfRel()
                .toMono().flatMapMany { link ->
                    paginator.paginate(
                        CreditAssembler(),
                        creditService.findCreditsByProductId(pId)
                            .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                            .filter { currentUser.id == it.traderId() }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        PageRequest.of(page, size), link, configureSortProperties(by, dir)
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
                        PageRequest.of(
                            params.getOrDefault("page", DEFAULT_SIZE).toInt(),
                            params.getOrDefault("size", DEFAULT_PAGE).toInt()
                        ),
                        link, configureSortProperties(
                            params.getOrDefault("dir", DEFAULT_SORT_ORDER),
                            params.getOrDefault("sort", DEFAULT_SORT_FIELD)
                        )
                    )
                }
        }
    }
}

