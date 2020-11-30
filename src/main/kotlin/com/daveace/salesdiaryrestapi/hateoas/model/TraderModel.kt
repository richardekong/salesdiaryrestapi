package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Product
import com.daveace.salesdiaryrestapi.domain.Trader
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(value = "trader", collectionRelation = "traders")
class TraderModel() : RepresentationModel<TraderModel>() {

    lateinit var id: String
    lateinit var email: String
    lateinit var name: String
    lateinit var phone: String
    lateinit var address: String
    lateinit var customers: MutableList<Customer>
    lateinit var products: MutableList<Product>
    lateinit var location: MutableList<Double>

    constructor(trader: Trader) : this() {
        this.id = trader.id
        this.email = trader.email
        this.name = trader.name
        this.phone = trader.phone
        this.address = trader.address
        this.customers = trader.customers
        this.products = trader.products
        this.location = trader.location
    }
}