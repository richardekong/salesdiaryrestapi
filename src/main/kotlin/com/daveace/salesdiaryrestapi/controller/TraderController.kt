package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_TRADERS
import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.exceptionhandling.NotFoundException
import com.daveace.salesdiaryrestapi.hateoas.assembler.CustomerModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.assembler.ProductModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.assembler.TraderModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.link.ReactiveLinkSupport
import com.daveace.salesdiaryrestapi.hateoas.model.CustomerModel
import com.daveace.salesdiaryrestapi.hateoas.model.ProductModel
import com.daveace.salesdiaryrestapi.hateoas.model.TraderModel
import com.daveace.salesdiaryrestapi.service.ReactiveCustomerService
import com.daveace.salesdiaryrestapi.service.ReactiveProductService
import com.daveace.salesdiaryrestapi.service.ReactiveTraderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.validation.Valid

@RestController
@RequestMapping(API)
class TraderController() : BaseController() {

    private lateinit var traderService: ReactiveTraderService
    private lateinit var productService: ReactiveProductService
    private lateinit var customerService: ReactiveCustomerService

    companion object {
        const val NEW_PRODUCT_MAIL_TEMPLATE = "new_product_mail_template"
        const val NEW_CUSTOMER_MAIL_TEMPLATE = "new_customer_mail_template"
        const val CUSTOMER_UPDATE_MAIL_TEMPLATE = "customer_update_mail_template"
        const val PRODUCT_UPDATE_MAIL_TEMPLATE = "product_update_mail_template"
        const val TRADER_UPDATE_MAIL_TEMPLATE = "trader_update_mail_template"
        const val TRADER_PRODUCT_DELETION_MAIL_TEMPLATE = "trader_product_deletion_mail_template"
        const val TRADER_CUSTOMER_DELETION_MAIL_TEMPLATE = "trader_customer_deletion_mail_template"
    }

    @Autowired
    constructor(
        traderService: ReactiveTraderService,
        productService: ReactiveProductService,
        customerService: ReactiveCustomerService
    ) : this() {
        this.traderService = traderService
        this.productService = productService
        this.customerService = customerService
    }

    @GetMapping("$SALES_DIARY_TRADER{email}")
    fun findTrader(@PathVariable email: String): Mono<TraderModel> {
        return authenticatedUser
            .ownsThisAccount(email)
            .flatMap { traderService.findTrader(email) }
            .flatMap { trader ->
                respondWithReactiveLink(
                    TraderModel(trader),
                    methodOn(this::class.java)
                        .findTrader(email)
                )
            }
    }

    @GetMapping(SALES_DIARY_TRADERS)
    fun findAllTraders(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<TraderModel>> {
        return linkTo(
            methodOn(this::class.java)
                .findAllTraders(params, principal)
        )
            .withSelfRel()
            .toMono()
            .flatMapMany { link ->
                paginator.paginate(
                    TraderModelAssembler(),
                    traderService.findAllTraders(),
                    specifyPageRequest(params), link,
                    configureSortProperties(params)
                )
            }
    }

    @PostMapping("$SALES_DIARY_TRADER{email}/products")
    fun addProduct(
        @PathVariable email: String, @RequestBody @Valid product: Product,
        principal: Principal,
        exchange: ServerWebExchange
    ): Mono<ProductModel> {
        return authenticatedUser
            .isCurrentUserAuthorizedByEmail(email, principal)
            .flatMap {
                val productToAdd = Product(
                    product.traderId, product.name, product.imagePath,
                    product.code, product.stock, product.cost
                )
                traderService.addProduct(email, productToAdd)
                    .flatMap {
                        respondWithReactiveLink(
                            ProductModel(it),
                            methodOn(this.javaClass)
                                .addProduct(email, product, principal, exchange)
                        )
                    }.doOnSuccess {
                        val addedAt: String = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                        traderService.findTrader(email).subscribe { trader ->
                            val recipientData: MutableMap<String, Any?> = mutableMapOf(
                                "product_model" to it,
                                "trader" to trader,
                                "added_at" to addedAt
                            )
                            prepareAndSendEmailWithHTMLTemplate(
                                NEW_PRODUCT_MAIL_TEMPLATE,
                                Mail(email, "New Product Addition"),
                                recipientData, exchange
                            )
                        }
                    }
            }
    }

    @PostMapping("$SALES_DIARY_TRADER{email}/customers")
    fun addCustomer(
        @PathVariable email: String,
        @RequestBody @Valid customer: Customer,
        principal: Principal,
        exchange: ServerWebExchange
    ): Mono<CustomerModel> {
        return authenticatedUser
            .isCurrentUserAuthorizedByEmail(email, principal)
            .flatMap {
                traderService.addCustomer(email, customer)
                    .flatMap { addedCustomer ->
                        respondWithReactiveLink(
                            CustomerModel(addedCustomer),
                            methodOn(this.javaClass)
                                .addCustomer(email, customer, principal, exchange)
                        )
                    }.doOnSuccess {
                        val dateOfAddition: String = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                        traderService.findTrader(email).subscribe { trader ->
                            val recipientData: MutableMap<String, Any?> = mutableMapOf(
                                "trader" to trader,
                                "customer" to it,
                                "added_at" to dateOfAddition
                            )
                            prepareAndSendEmailWithHTMLTemplate(
                                NEW_CUSTOMER_MAIL_TEMPLATE,
                                Mail(email, "New Customer Addition"),
                                recipientData, exchange
                            )
                        }
                    }
            }
    }

    @GetMapping("$SALES_DIARY_TRADER{traderId}/products/{productId}")
    fun findTraderProduct(
        @PathVariable traderId: String,
        @PathVariable productId: String,
        principal: Principal,
        exchange: ServerWebExchange
    ): Mono<ProductModel> {
        return authenticatedUser
            .isCurrentUserAuthorizedById(traderId, principal)
            .flatMap {
                traderService.findTraderProduct(traderId, productId)
                    .flatMap { product ->
                        respondWithReactiveLink(
                            ProductModel(product),
                            methodOn(this.javaClass)
                                .findTraderProduct(traderId, productId, principal, exchange)
                        )
                    }
            }
    }

    @GetMapping("$SALES_DIARY_TRADER{traderId}/products")
    fun findTraderProducts(
        @PathVariable traderId: String,
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<ProductModel>> {

        return authenticatedUser
            .isCurrentUserAuthorizedById(traderId, principal)
            .flatMapMany {
                linkTo(
                    methodOn(this.javaClass)
                        .findTraderProducts(traderId, params, principal)
                )
                    .withSelfRel()
                    .toMono()
                    .flatMapMany { link ->
                        paginator.paginate(
                            ProductModelAssembler(),
                            traderService.findTraderProducts(traderId),
                            specifyPageRequest(params), link,
                            configureSortProperties(params)
                        )
                    }
            }

    }

    @GetMapping("$SALES_DIARY_TRADER{id}/customers/{email}")
    fun findTraderCustomer(
        @PathVariable id: String,
        @PathVariable email: String,
        principal: Principal
    ): Mono<CustomerModel> {
        return authenticatedUser
            .isCurrentUserAuthorizedById(id, principal)
            .flatMap {
                traderService.findTraderCustomer(id, email)
                    .flatMap {
                        respondWithReactiveLink(
                            CustomerModel(it),
                            methodOn(this.javaClass)
                                .findTraderCustomer(id, email, principal)
                        )
                    }
            }

    }

    @GetMapping("$SALES_DIARY_TRADER{id}/customers")
    fun findTraderCustomers(
        @PathVariable id: String,
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<CustomerModel>> {

        return authenticatedUser.isCurrentUserAuthorizedById(id, principal)
            .flatMapMany {
                linkTo(
                    methodOn(this.javaClass)
                        .findTraderCustomers(id, params, principal)
                )
                    .withSelfRel()
                    .toMono()
                    .flatMapMany { link ->
                        paginator.paginate(
                            CustomerModelAssembler(),
                            traderService.findTraderCustomers(id),
                            specifyPageRequest(params), link,
                            configureSortProperties(params)
                        )
                    }
            }
    }

    @PatchMapping(SALES_DIARY_TRADERS)
    fun <V> updateTrader(
        @RequestBody trader: MutableMap<String, V>,
        principal: Principal,
        exchange: ServerWebExchange
    ): Mono<TraderModel> {
        return authenticatedUser
            .getCurrentUser()
            .flatMap { currentUser ->
                traderService.updateTrader(currentUser.id, trader)
                    .flatMap { updatedTrader ->
                        respondWithReactiveLink(
                            TraderModel(updatedTrader),
                            methodOn(this.javaClass)
                                .updateTrader(trader, principal, exchange)
                        )
                    }.doOnSuccess {
                        val updatedAt = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                        val recipientData: MutableMap<String, Any?> = mutableMapOf(
                            "trader_model" to it, "updated_at" to updatedAt
                        )
                        prepareAndSendEmailWithHTMLTemplate(
                            TRADER_UPDATE_MAIL_TEMPLATE,
                            Mail(it.email, "Trader Update"),
                            recipientData,
                            exchange
                        )
                    }
            }
    }

    @PatchMapping("$SALES_DIARY_TRADER{id}/customers/{cEmail}")
    fun <V> updateTraderCustomer(
        @PathVariable id: String,
        @PathVariable cEmail: String,
        @RequestBody customer: MutableMap<String, V>,
        principal: Principal,
        exchange: ServerWebExchange
    ): Mono<CustomerModel> {
        return authenticatedUser
            .isCurrentUserAuthorizedById(id, principal)
            .flatMap { traderService.findTraderCustomer(id, cEmail) }
            .flatMap {
                traderService.updateTraderCustomer(id, customer, cEmail)
                    .flatMap {
                        respondWithReactiveLink(
                            CustomerModel(it), methodOn(this.javaClass)
                                .updateTraderCustomer(id, cEmail, customer, principal, exchange)
                        )
                    }
            }.doOnSuccess {
                traderService.findTraderById(id).subscribe { trader ->
                    val updatedAt = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
                    )
                    val recipientData: MutableMap<String, Any?> = mutableMapOf(
                        "trader" to trader, "customer" to it,
                        "updated_at" to updatedAt
                    )
                    prepareAndSendEmailWithHTMLTemplate(
                        CUSTOMER_UPDATE_MAIL_TEMPLATE,
                        Mail(trader.email, "Customer Update"),
                        recipientData,
                        exchange
                    )
                }
            }
    }

    @PatchMapping("$SALES_DIARY_TRADER{tId}/products/{pId}")
    fun <V> updateTraderProduct(
        @PathVariable tId: String,
        @PathVariable pId: String,
        @RequestBody product: MutableMap<String, V>,
        principal: Principal,
        exchange: ServerWebExchange
    ): Mono<ProductModel> {
        return authenticatedUser
            .isCurrentUserAuthorizedById(tId, principal)
            .flatMap { traderService.findTraderProduct(tId, pId) }
            .flatMap {
                traderService.updateTraderProduct(tId, product, pId)
                    .flatMap {
                        respondWithReactiveLink(
                            ProductModel(it), methodOn(this.javaClass)
                                .updateTraderProduct(tId, pId, product, principal, exchange)
                        )
                    }.doOnSuccess {
                        traderService.findTraderById(tId).subscribe { trader ->
                            val updatedAt = LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)
                            )
                            val recipientData: MutableMap<String, Any?> = mutableMapOf(
                                "product" to it, "trader" to trader,
                                "updated_at" to updatedAt
                            )
                            prepareAndSendEmailWithHTMLTemplate(
                                PRODUCT_UPDATE_MAIL_TEMPLATE,
                                Mail(trader.email, "Product Update"),
                                recipientData,
                                exchange
                            )
                        }
                    }
            }
    }

    @DeleteMapping("$SALES_DIARY_TRADER{tId}/products/{pId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    fun deleteTraderProduct(
        @PathVariable tId: String,
        @PathVariable pId: String,
        principal: Principal,
        exchange: ServerWebExchange
    ): Mono<Void> {
        val deletedProduct: Product = productService.findProduct(pId)
            .switchIfEmpty(Mono.fromRunnable {
                throw NotFoundException(HttpStatus.NOT_FOUND.reasonPhrase)
            }).toFuture().join()
        val trader: Trader = traderService.findTraderById(tId)
            .switchIfEmpty(Mono.fromRunnable {
                throw NotFoundException(HttpStatus.NOT_FOUND.reasonPhrase)
            }).toFuture().join()
        return authenticatedUser
            .isCurrentUserAuthorizedById(tId, principal)
            .flatMap { traderService.deleteTraderProduct(tId, pId) }
            .doOnSuccess {
                val deletedAt = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                val recipientData: MutableMap<String, Any?> = mutableMapOf(
                    "trader" to trader, "deleted_product" to deletedProduct,
                    "deleted_at" to deletedAt
                )
                prepareAndSendEmailWithHTMLTemplate(
                    TRADER_PRODUCT_DELETION_MAIL_TEMPLATE,
                    Mail(trader.email, "Product Deletion"),
                    recipientData,
                    exchange
                )
            }

    }

    @DeleteMapping("$SALES_DIARY_TRADER{tId}/customers/{cEmail}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    fun deleteTraderCustomer(
        @PathVariable tId: String,
        @PathVariable cEmail: String,
        principal: Principal,
        exchange: ServerWebExchange
    ): Mono<Void> {
        val trader: Trader = traderService.findTraderById(tId)
            .switchIfEmpty(Mono.fromRunnable {
                throw NotFoundException(HttpStatus.NOT_FOUND.reasonPhrase)
            }).toFuture().join()
        val deletedCustomer: Customer = customerService.findCustomerByEmail(cEmail)
            .switchIfEmpty(Mono.fromRunnable {
                throw NotFoundException(HttpStatus.NOT_FOUND.reasonPhrase)
            }).toFuture().join()
        return authenticatedUser
            .isCurrentUserAuthorizedById(tId, principal)
            .flatMap { traderService.deleteTraderCustomer(tId, cEmail) }
            .doOnSuccess {
                val deleteAt = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                val recipientData: MutableMap<String, Any?> = mutableMapOf(
                    "trader" to trader, "deleted_customer" to deletedCustomer,
                    "deleted_at" to deleteAt
                )
                prepareAndSendEmailWithHTMLTemplate(
                    TRADER_CUSTOMER_DELETION_MAIL_TEMPLATE,
                    Mail(trader.email, "Customer Deletion"),
                    recipientData,
                    exchange
                )
            }
    }

}

