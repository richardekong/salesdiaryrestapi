package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_PRODUCT
import com.daveace.salesdiaryrestapi.hateoas.link.ReactiveLinkSupport
import com.daveace.salesdiaryrestapi.hateoas.model.ProductModel
import com.daveace.salesdiaryrestapi.service.ReactiveProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(API)
class ProductController : ReactiveLinkSupport {

    @Autowired
    private lateinit var productService: ReactiveProductService

    @GetMapping("$SALES_DIARY_PRODUCT{id}")
    fun findProduct(@PathVariable id: String): Mono<ProductModel> {
        return productService.findProduct(id)
                .flatMap {
                    respondWithReactiveLink(ProductModel(it),
                            methodOn(this::class.java).findProduct(id))
                }
    }

}