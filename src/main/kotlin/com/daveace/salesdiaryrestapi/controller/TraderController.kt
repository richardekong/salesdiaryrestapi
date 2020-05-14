package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.configuration.SortConfigurationProperties
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADERS
import com.daveace.salesdiaryrestapi.controller.UserController.Companion.DEFAULT_SORT_FIELD
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.hateoas.assembler.TraderModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.link.ReactiveLinkSupport
import com.daveace.salesdiaryrestapi.hateoas.model.TraderModel
import com.daveace.salesdiaryrestapi.page.Paginator
import com.daveace.salesdiaryrestapi.service.ReactiveTraderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping(API)
class TraderController() : ReactiveLinkSupport {

    private lateinit var traderService: ReactiveTraderService
    private lateinit var paginator: Paginator
    private lateinit var sortProperties: SortConfigurationProperties
    private lateinit var tokenUtil: TokenUtil
    private lateinit var authenticatedUser: AuthenticatedUser

    companion object {
        const val DEFAULT_SIZE = "1"
        const val DEFAULT_PAGE = "0"
        const val DEFAULT_SORT_ORDER = "asc"
    }

    @Autowired
    constructor(
            traderService: ReactiveTraderService,
            paginator: Paginator,
            sortProperties: SortConfigurationProperties,
            tokenUtil: TokenUtil,
            authenticatedUser: AuthenticatedUser) : this() {
        this.traderService = traderService
        this.paginator = paginator
        this.sortProperties = sortProperties
        this.tokenUtil = tokenUtil
        this.authenticatedUser = authenticatedUser
    }

    @GetMapping("$SALES_DIARY_TRADER{email}")
    fun findTrader(@PathVariable email: String): Mono<TraderModel> {
        return authenticatedUser.ownsThisAccount(email)
                .flatMap { traderService.findTrader(email) }
                .flatMap { trader ->
                    respondWithReactiveLink(TraderModel(trader),
                            methodOn(this::class.java).findTrader(email))
                }
    }

    @GetMapping(SALES_DIARY_TRADERS)
    fun findAllTraders(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<TraderModel>> {
        sortProperties.by = by
        sortProperties.dir = dir

        return linkTo(methodOn(this::class.java)
                .findAllTraders(size, page, by, dir))
                .withSelfRel()
                .toMono()
                .flatMapMany { link ->
                    paginator.paginate(
                            TraderModelAssembler(),
                            traderService.findAllTraders(),
                            PageRequest.of(page, size),
                            link, sortProperties)
                }
    }

}

