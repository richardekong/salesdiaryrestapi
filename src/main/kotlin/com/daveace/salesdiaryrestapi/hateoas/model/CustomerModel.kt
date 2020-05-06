package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.Customer
import org.springframework.data.geo.Point
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(value ="customer", collectionRelation="customers")
data class CustomerModel(val customer: Customer) : RepresentationModel<CustomerModel>() {

    private val name: String = customer.name
    private val email: String = customer.email
    private val traderId: String = customer.traderId
    private val company: String = customer.company
    private val signaturePath: String = customer.signaturePath
    private val address: String = customer.address
    private val location:DoubleArray= customer.location

}