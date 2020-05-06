package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.SalesEvent
import org.springframework.data.geo.Point
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.util.Date

@Relation(value ="event", collectionRelation = "events")
data class SalesEventModel(val event: SalesEvent) : RepresentationModel<SalesEventModel>() {

    private val id: String = event.id
    private val traderId: String = event.traderId
    private val productId: String = event.productId
    private val price: Double = event.price
    private val sales: Double = event.sales
    private val left: Double = event.left
    private val date: Date = event.date
    private val location: Point = event.location

}