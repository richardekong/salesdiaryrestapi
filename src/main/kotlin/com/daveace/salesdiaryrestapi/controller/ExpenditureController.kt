package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DAIRY_EXPS
import com.daveace.salesdiaryrestapi.domain.Expenditure
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.exceptionhandling.NotFoundException
import com.daveace.salesdiaryrestapi.hateoas.assembler.ExpenditureModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.model.EmptyModel
import com.daveace.salesdiaryrestapi.hateoas.model.ExpenditureModel
import com.daveace.salesdiaryrestapi.service.ReactiveExpenditureService
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
class ExpenditureController : BaseController() {

    private lateinit var expenditureService: ReactiveExpenditureService

    @Autowired
    fun initExpenditureService(expenditureService: ReactiveExpenditureService) {
        this.expenditureService = expenditureService
    }

    @PostMapping(SALES_DAIRY_EXPS)
    @ResponseStatus(HttpStatus.CREATED)
    fun recordExpenditure(
        @Valid @RequestBody entries: MutableMap<String, Double>, principal: Principal
    ): Mono<ExpenditureModel> {
        return authenticatedUser.getCurrentUser(principal)
            .flatMap { currentUser ->
                expenditureService.createExpenditure(currentUser.id, entries)
            }.flatMap {
                respondWithReactiveLink(
                    ExpenditureModel(it),
                    methodOn(this.javaClass).recordExpenditure(entries, principal)
                )
            }
    }

    @GetMapping("$SALES_DAIRY_EXPS/{id}")
    fun findExpenditureById(@PathVariable id: String): Mono<ExpenditureModel> {
        return expenditureService.findExpenditureById(id).flatMap {
            respondWithReactiveLink(ExpenditureModel(it), methodOn(this.javaClass).findExpenditureById(id))
        }
    }

    @GetMapping("$SALES_DAIRY_EXPS/{traderId}")
    fun findExpenditureByTraderId(@PathVariable traderId: String): Mono<ExpenditureModel> {
        return expenditureService.findExpenditureByTraderId(traderId).flatMap {
            respondWithReactiveLink(
                ExpenditureModel(it),
                methodOn(this.javaClass).findExpenditureByTraderId(traderId)
            )
        }
    }

    @GetMapping(SALES_DAIRY_EXPS)
    fun findExpenditures(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<ExpenditureModel>> {
        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findExpenditures(params, principal))
                .withSelfRel().toMono()
                .flatMapMany { link ->
                    paginator.paginate(
                        ExpenditureModelAssembler(),
                        expenditureService.findExpenditures()
                            .filter { currentUser.id == it.traderId }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        specifyPageRequest(params), link, configureSortProperties(params)
                    )
                }
        }
    }

    @GetMapping("$SALES_DAIRY_EXPS/{dateString}")
    fun findExpendituresByDate(
        @PathVariable dateString: String,
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<ExpenditureModel>> {
        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findExpendituresByDate(dateString, params, principal))
                .withSelfRel().toMono().flatMapMany { link ->
                    paginator.paginate(
                        ExpenditureModelAssembler(),
                        expenditureService.findExpenditures(dateString)
                            .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                            .filter { currentUser.id == it.traderId }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        specifyPageRequest(params),
                        link, configureSortProperties(params)
                    )
                }
        }
    }

    @GetMapping("$SALES_DAIRY_EXPS-by-dates")
    fun findExpenditureByDates(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<ExpenditureModel>> {
        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findExpenditureByDates(params, principal))
                .withSelfRel().toMono()
                .flatMapMany { link ->
                    paginator.paginate(
                        ExpenditureModelAssembler(),
                        expenditureService.findExpenditures(
                            params.getOrDefault("start", ""),
                            params.getOrDefault("end", "")
                        )
                            .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                            .filter { currentUser.id == it.traderId }
                            .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() }),
                        specifyPageRequest(params), link, configureSortProperties(params)
                    )
                }
        }
    }

    @PatchMapping("$SALES_DAIRY_EXPS/{id}")
    fun editExpenditureById(
        @PathVariable id: String,
        @RequestParam(name = "desc", required = true) desc: String,
        @RequestBody expense: Expenditure.Expense,
        principal: Principal
    ): Mono<ExpenditureModel> {

        return authenticatedUser.getCurrentUser().flatMap { currentUser ->
            expenditureService.editExpenditureById(id, desc, expense)
                .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                .filter { currentUser.id == it.traderId }
                .switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() })
                .flatMap {
                    respondWithReactiveLink(
                        ExpenditureModel(it),
                        methodOn(this.javaClass).editExpenditureById(id, desc, expense, principal)
                    )
                }
        }
    }

    @DeleteMapping("$SALES_DAIRY_EXPS/{id}")
    fun deleteRecord(@PathVariable id: String, principal: Principal): Mono<EmptyModel> {
        return authenticatedUser.getCurrentUser(principal).flatMap { currentUser ->
            expenditureService.findExpenditureById(id)
                .switchIfEmpty(Mono.fromRunnable { throw NotFoundException() })
                .filter {
                    currentUser.id == it.traderId
                }.switchIfEmpty(Mono.fromRunnable { throw AuthenticationException() })
            expenditureService.deleteExpenditureById(id).flatMap {
                respondWithReactiveLink(EmptyModel(), methodOn(this.javaClass).deleteRecord(id, principal))
            }
        }
    }

}