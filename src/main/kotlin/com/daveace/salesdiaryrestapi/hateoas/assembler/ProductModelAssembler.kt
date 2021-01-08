package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.CreditController
import com.daveace.salesdiaryrestapi.controller.ProductController
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.hateoas.model.ProductModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn

class ProductModelAssembler : RepresentationModelAssemblerSupport<Product, ProductModel>(
    ProductController::class.java,
    ProductModel::class.java
) {
    override fun toModel(product: Product): ProductModel {
        return instantiateModel(product).apply {
            linkTo(methodOn(CreditController::class.java).findCredit(product.id))
                .withSelfRel()
                .toMono()
                .subscribe { add(it) }
        }
    }

    override fun toCollectionModel(product: MutableIterable<Product>): CollectionModel<ProductModel> {
        return super.toCollectionModel(product)
    }

    override fun instantiateModel(product: Product): ProductModel {
        return ProductModel(product)
    }
}