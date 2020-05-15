package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.Trader
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(value = "trader", collectionRelation = "traders")
class TraderModel() : RepresentationModel<TraderModel>() {

    lateinit var email: String
    lateinit var name: String
    lateinit var address: String
    lateinit var location: Array<Double>

    constructor(trader: Trader) : this() {
        this.email = trader.email
        this.name = trader.name
        this.address = trader.address
        this.location = trader.location!!
    }
}