package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.Trader
import org.springframework.data.geo.Point
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(value="trader", collectionRelation="traders")
data class TraderModel(val trader: Trader) : RepresentationModel<TraderModel>() {
    private val email: String = trader.email
    private val name: String = trader.name
    private val address: String = trader.address
    private val location:DoubleArray = trader.location
}