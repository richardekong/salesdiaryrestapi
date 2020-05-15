package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.Product
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.util.*

@Relation(value="product", collectionRelation="products")
class ProductModel() : RepresentationModel<ProductModel>() {

    lateinit var id: String
    lateinit var traderId: String
    lateinit var code: String
    lateinit var imagePath: String
    lateinit var date : Date
    var cost : Double = 0.0
    var stock : Double = 0.0

    constructor(product:Product):this(){
        this.id = product.id
        this.traderId = product.traderId
        this.code = product.code
        this.imagePath = product.imagePath
        this.date = product.date
        this.cost = product.cost
        this.stock = product.stock
    }

}