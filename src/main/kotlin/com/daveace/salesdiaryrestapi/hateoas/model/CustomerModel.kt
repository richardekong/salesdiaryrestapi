package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.domain.Product
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(value = "customer", collectionRelation = "customers")
class CustomerModel() : RepresentationModel<CustomerModel>() {

    lateinit var id: String
    lateinit var name: String
    lateinit var email: String
    lateinit var traderId: String
    lateinit var company: String
    lateinit var signaturePath: String
    lateinit var address: String
    lateinit var products: MutableList<Product>
    lateinit var location: MutableList<Double>


    constructor(customer: Customer) : this() {
        this.id = customer.id
        this.name = customer.name
        this.email = customer.email
        this.traderId = customer.traderId
        this.company = customer.company
        this.signaturePath = customer.signaturePath
        this.address = customer.address
        this.products = customer.products
        this.location = customer.location
    }

}