package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.authentication.AuthenticatedUser
import com.daveace.salesdiaryrestapi.configuration.SortConfigurationProperties
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMERS
import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.exceptionhandling.RestException
import com.daveace.salesdiaryrestapi.hateoas.assembler.CustomerModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.link.ReactiveLinkSupport
import com.daveace.salesdiaryrestapi.hateoas.model.CustomerModel
import com.daveace.salesdiaryrestapi.page.Paginator
import com.daveace.salesdiaryrestapi.service.ReactiveCustomerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping(API)
class CustomerController() : ReactiveLinkSupport {

    private lateinit var customerService: ReactiveCustomerService
    private lateinit var paginator: Paginator
    private lateinit var sortProps: SortConfigurationProperties
    private lateinit var authenticatedUser: AuthenticatedUser

    companion object {
        const val DEFAULT_SIZE = "1"
        const val DEFAULT_PAGE = "0"
        const val DEFAULT_SORT_FIELD = "email"
        const val DEFAULT_SORT_ORDER = "asc"
    }

    @Autowired
    constructor(
            customerService: ReactiveCustomerService,
            paginator: Paginator,
            sortProps: SortConfigurationProperties,
            authenticatedUser: AuthenticatedUser) : this() {
        this.customerService = customerService
        this.paginator = paginator
        this.sortProps = sortProps
        this.authenticatedUser = authenticatedUser
    }

    @GetMapping("$SALES_DIARY_CUSTOMER{email}")
    fun findCustomerByEmail(@PathVariable email: String): Mono<CustomerModel> {
        return authenticatedUser.getCurrentUser()
                .flatMap { currentUser ->
                    customerService.findCustomerByEmail(email)
                            .filter { customer ->
                                isAnAuthorizedCustomerOrTrader(
                                        email, currentUser, customer)
                            }
                            .flatMap { customer ->
                                respondWithReactiveLink(
                                        CustomerModel(customer),
                                        methodOn(this.javaClass)
                                                .findCustomerByEmail(email))
                            }
                            .switchIfEmpty(Mono.fromRunnable {
                                throw RestException(HttpStatus
                                        .UNAUTHORIZED.reasonPhrase)
                            })
                }
    }

    @GetMapping(SALES_DIARY_CUSTOMERS)
    fun findAllCustomers(
            @RequestParam(name = "size", defaultValue = DEFAULT_SIZE) size: Int,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE) page: Int,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT_FIELD) by: String,
            @RequestParam(name = "dir", defaultValue = DEFAULT_SORT_ORDER) dir: String
    ): Flux<PagedModel<CustomerModel>> {
        sortProps.by = by
        sortProps.dir = dir

        val customerFlux: Flux<Customer> = authenticatedUser
                .getCurrentUser()
                .flatMapMany { currentUser ->
                    customerService.findAllCustomers()
                            .filter { usersCustomer ->
                                isAnAuthorizedTrader(currentUser, usersCustomer)
                            }
                            .switchIfEmpty(Mono.fromRunnable {
                                throw RestException(HttpStatus.UNAUTHORIZED
                                        .reasonPhrase)
                            })
                }

        return linkTo(methodOn(this.javaClass)
                .findAllCustomers(size, page, by, dir))
                .withSelfRel()
                .toMono().flatMapMany { link ->
                    paginator.paginate(
                            CustomerModelAssembler(),
                            customerFlux,
                            PageRequest.of(page, size),
                            link, sortProps)
                }
    }

    private fun isAnAuthorizedCustomerOrTrader(email: String, currentUser: User, customer: Customer): Boolean {
        return (customer.email == email) || isAnAuthorizedTrader(currentUser, customer)
    }

    private fun isAnAuthorizedTrader(currentUser: User, customer: Customer): Boolean {
        return currentUser.trader != null && currentUser.email == customer.traderId
    }


}


