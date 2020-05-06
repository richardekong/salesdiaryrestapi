package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.Product
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.util.Date

@Relation(value="product", collectionRelation="products")
data class ProductModel(val product: Product) : RepresentationModel<ProductModel>() {

    private val id: String = product.id
    private val traderId: String = product.traderId
    private val code: String = product.code
    private val imagePath: String = product.imagePath
    private val date : Date = product.date
    private val cost : Double = product.cost
    private val stock : Double = product.stock

}