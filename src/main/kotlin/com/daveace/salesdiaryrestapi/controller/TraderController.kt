package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.authentication.TokenUtil
import com.daveace.salesdiaryrestapi.configuration.SortConfigurationProperties
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADERS
import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.hateoas.assembler.CustomerModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.assembler.ProductModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.assembler.TraderModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.link.ReactiveLinkSupport
import com.daveace.salesdiaryrestapi.hateoas.model.CustomerModel
import com.daveace.salesdiaryrestapi.hateoas.model.ProductModel
import com.daveace.salesdiaryrestapi.hateoas.model.TraderModel
import com.daveace.salesdiaryrestapi.page.Paginator
import com.daveace.salesdiaryrestapi.service.ReactiveTraderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

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
        const val DEFAULT_SORT_FIELD = "id"
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

        return linkTo(methodOn(this::class.java)
                .findAllTraders(size, page, by, dir))
                .withSelfRel()
                .toMono()
                .flatMapMany { link ->
                    paginator.paginate(TraderModelAssembler(),
                            traderService.findAllTraders(),
                            PageRequest.of(page, size), link,
                            configureSortProperties(by, dir))
                }
    }

    @PostMapping("$SALES_DIARY_TRADER{email}/products")
    fun addProduct(@PathVariable email: String, @RequestBody @Valid product: Product): Mono<ProductModel> {

        return traderService.addProduct(email, product).flatMap {
            respondWithReactiveLink(ProductModel(it), methodOn(this.javaClass).addProduct(email, product))
        }
    }

    @PostMapping("$SALES_DIARY_TRADER{email}/customers")
    fun addCustomer(@PathVariable email: String, @RequestBody @Valid customer: Customer): Mono<CustomerModel> {
        return traderService.addCustomer(email, customer).flatMap {
            respondWithReactiveLink(CustomerModel(it), methodOn(this.javaClass).addCustomer(email, customer))
        }
    }

    @GetMapping("$SALES_DIARY_TRADER{traderId}/products/{productId}")
    fun findTraderProduct(@PathVariable traderId: String, @PathVariable productId: String): Mono<ProductModel> {
        return traderService.findTraderProduct(traderId, productId).flatMap {
            respondWithReactiveLink(ProductModel(it), methodOn(this.javaClass).findTraderProduct(traderId, productId))
        }
    }

    @GetMapping("$SALES_DIARY_TRADER{traderId}/products")
    fun findTraderProducts(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String,
            @PathVariable traderId: String): Flux<PagedModel<ProductModel>> {

        return linkTo(methodOn(this.javaClass).findTraderProducts(
                size, page, by, dir, traderId
        )).withSelfRel().toMono().flatMapMany { link ->
            paginator.paginate(ProductModelAssembler(),
                    traderService.findTraderProducts(traderId),
                    PageRequest.of(page, size), link,
                    configureSortProperties(by, dir))
        }
    }

    @GetMapping("$SALES_DIARY_TRADER{id}/customers/{email}")
    fun findTraderCustomer(@PathVariable id: String, @PathVariable email: String): Mono<CustomerModel> {
        return traderService.findTraderCustomer(id, email).flatMap {
            respondWithReactiveLink(CustomerModel(it), methodOn(this.javaClass).findTraderCustomer(id, email))
        }
    }

    @GetMapping("$SALES_DIARY_TRADER{id}/customers")
    fun findTraderCustomers(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String,
            @PathVariable id: String): Flux<PagedModel<CustomerModel>> {

        return linkTo(methodOn(this.javaClass).findTraderCustomers(
                size, page, by, dir, id)).withSelfRel().toMono().flatMapMany { link ->
            paginator.paginate(CustomerModelAssembler(),
                    traderService.findTraderCustomers(id),
                    PageRequest.of(page, size), link,
                    configureSortProperties(by, dir))
        }
    }

    @PatchMapping(SALES_DIARY_TRADERS)
    fun <V> updateTrader(@RequestBody trader: MutableMap<String, V>): Mono<TraderModel> {
        return traderService.updateTrader(trader).flatMap {
            respondWithReactiveLink(TraderModel(it), methodOn(this.javaClass).updateTrader(trader))
        }
    }

    @PatchMapping("$SALES_DIARY_TRADER{id}/customers/{cEmail}")
    fun <V> updateTraderCustomer(@PathVariable id: String, @PathVariable cEmail: String, @RequestBody customer: MutableMap<String, V>): Mono<CustomerModel> {
        return traderService.updateTraderCustomer(id, customer, cEmail).flatMap {
            respondWithReactiveLink(CustomerModel(it), methodOn(this.javaClass)
                    .updateTraderCustomer(id, cEmail, customer))
        }
    }

    @PatchMapping("$SALES_DIARY_TRADER{tId}/products/{pId}")
    fun <V> updateTraderProduct(@PathVariable tId: String, @PathVariable pId: String, @RequestBody product: MutableMap<String, V>): Mono<ProductModel> {
        return traderService.updateTraderProduct(tId, product, pId).flatMap {
            respondWithReactiveLink(ProductModel(it), methodOn(this.javaClass)
                    .updateTraderProduct(tId, pId, product))
        }
    }

    @DeleteMapping("$SALES_DIARY_TRADER{tId}/products/{pId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    fun deleteTraderProduct(@PathVariable tId: String, @PathVariable pId: String): Mono<Void> {
        return traderService.deleteTraderProduct(tId, pId)
    }

    @DeleteMapping("$SALES_DIARY_TRADER{tId}/customers/{cEmail}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    fun deleteTraderCustomer(@PathVariable tId: String, @PathVariable cEmail: String): Mono<Void> {
        return traderService.deleteTraderCustomer(tId, cEmail)
    }

    private fun configureSortProperties(by: String, dir: String): SortConfigurationProperties {
        sortProperties.by = by
        sortProperties.dir = dir
        return sortProperties
    }

}

