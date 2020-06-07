package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.configuration.SortConfigurationProperties
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMER
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_CUSTOMERS
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
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping(API)
class CustomerController() : ReactiveLinkSupport {

    private lateinit var customerService: ReactiveCustomerService
    private lateinit var paginator: Paginator
    private lateinit var sortProps: SortConfigurationProperties

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
            sortProps: SortConfigurationProperties) : this() {
        this.customerService = customerService
        this.paginator = paginator
        this.sortProps = sortProps
    }

    @GetMapping("$SALES_DIARY_CUSTOMER{email}")
    fun findCustomerByEmail(@PathVariable email:String):Mono<CustomerModel>{
        return customerService.findCustomerByEmail(email).flatMap {
            respondWithReactiveLink(CustomerModel(it), methodOn(this.javaClass).findCustomerByEmail(email))
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

        return linkTo(methodOn(this.javaClass)
                .findAllCustomers(size, page, by, dir))
                .withSelfRel()
                .toMono().flatMapMany { link ->
                    paginator.paginate(
                            CustomerModelAssembler(),
                            customerService.findAllCustomers(),
                            PageRequest.of(page, size), link, sortProps)
                }
    }
}

