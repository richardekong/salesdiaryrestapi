package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import org.springframework.data.geo.Point
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.util.Date

@Relation(value = "event", collectionRelation = "events")
class SalesEventModel() : RepresentationModel<SalesEventModel>() {

    lateinit var id: String
    lateinit var traderId: String
    lateinit var productId: String
    var price: Double = 0.0
    var sales: Double = 0.0
    var left: Double = 0.0
    lateinit var date: Date
    lateinit var location: Point

    constructor(event: SalesEvent) : this() {
        this.id = event.id
        this.traderId = event.traderId
        this.productId = event.productId
        this.price = event.price
        this.sales = event.sales
        this.left = event.left
        this.date = event.date
        this.location = event.location
    }

}