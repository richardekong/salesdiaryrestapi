package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMERS
import com.daveace.salesdiaryrestapi.hateoas.assembler.CustomerModelAssembler
import com.daveace.salesdiaryrestapi.hateoas.model.CustomerModel
import com.daveace.salesdiaryrestapi.service.ReactiveCustomerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.security.Principal

@RestController
@RequestMapping(API)
class CustomerController() : BaseController() {

    private lateinit var customerService: ReactiveCustomerService

    @Autowired
    constructor(customerService: ReactiveCustomerService) : this() {
        this.customerService = customerService
    }

    @GetMapping("$SALES_DIARY_CUSTOMER{email}")
    fun findCustomerByEmail(@PathVariable email: String): Mono<CustomerModel> {
        return customerService.findCustomerByEmail(email).flatMap {
            respondWithReactiveLink(CustomerModel(it), methodOn(this.javaClass).findCustomerByEmail(email))
        }
    }

    @GetMapping(SALES_DIARY_CUSTOMERS)
    fun findAllCustomers(
        @RequestParam params: MutableMap<String, String>,
        principal: Principal
    ): Flux<PagedModel<CustomerModel>> {
        return authenticatedUser.getCurrentUser(principal).flatMapMany { currentUser ->
            linkTo(methodOn(this.javaClass).findAllCustomers(params, principal))
                .withSelfRel()
                .toMono().flatMapMany { link ->
                    paginator.paginate(
                        CustomerModelAssembler(),
                        customerService.findAllCustomers().filter { currentUser.id == it.traderId },
                        specifyPageRequest(params), link, configureSortProperties(params)
                    )
                }
        }
    }
}

