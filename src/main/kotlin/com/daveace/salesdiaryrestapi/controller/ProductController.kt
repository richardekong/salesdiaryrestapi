package com.daveace.salesdiaryrestapi.controller

import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.API
import com.daveace.salesdiaryrestapi.controller.ControllerPath.Companion.SALES_DIARY_PRODUCT
import com.daveace.salesdiaryrestapi.exceptionhandling.AuthenticationException
import com.daveace.salesdiaryrestapi.hateoas.link.ReactiveLinkSupport
import com.daveace.salesdiaryrestapi.hateoas.model.ProductModel
import com.daveace.salesdiaryrestapi.service.ReactiveProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.security.Principal

@RestController
@RequestMapping(API)
class ProductController : BaseController() {

    @Autowired
    private lateinit var productService: ReactiveProductService

    @GetMapping("$SALES_DIARY_PRODUCT{id}")
    fun findProduct(@PathVariable id: String): Mono<ProductModel> {
        return authenticatedUser.getCurrentUser()
                .flatMap { currentUser ->
                    productService.findProduct(id)
                            .filter { product -> currentUser.id == product.traderId }
                            .switchIfEmpty(Mono.fromRunnable {
                                throw AuthenticationException(HttpStatus.UNAUTHORIZED.reasonPhrase)
                            })
                            .flatMap { product ->
                                respondWithReactiveLink(ProductModel(product),
                                        methodOn(this.javaClass).findProduct(id))
                            }
                }
    }

}