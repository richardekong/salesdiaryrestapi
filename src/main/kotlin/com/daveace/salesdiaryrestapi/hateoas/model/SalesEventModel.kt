package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDate

@Relation(value = "event", collectionRelation = "events")
class SalesEventModel() : RepresentationModel<SalesEventModel>() {

    lateinit var id: String
    lateinit var traderId: String
    lateinit var customerId:String
    lateinit var productId: String
    lateinit var name:String
    var salesPrice: Double = 0.0
    var quantitySold: Double = 0.0
    var costPrice:Double = 0.0
    var left: Double = 0.0
    lateinit var date: LocalDate
    lateinit var location: MutableList<Double>

    constructor(event: SalesEvent) : this() {
        this.id = event.id
        this.traderId = event.traderId
        this.customerId = event.customerId
        this.productId = event.productId
        this.name = event.product
        this.costPrice = event.costPrice
        this.salesPrice = event.salesPrice
        this.quantitySold = event.quantitySold
        this.left = event.left
        this.date = event.date
        this.location = event.location
    }

}