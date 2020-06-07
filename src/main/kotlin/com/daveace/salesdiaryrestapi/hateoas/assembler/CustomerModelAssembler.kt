package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.CustomerController
import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.hateoas.model.CustomerModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn

class CustomerModelAssembler : RepresentationModelAssemblerSupport<Customer, CustomerModel>
(CustomerController::class.java, CustomerModel::class.java) {

    override fun toModel(customer: Customer): CustomerModel {
        return instantiateModel(customer)
                .add(linkTo(methodOn(CustomerController::class.java)
                        .findCustomerByEmail(customer.email))
                        .withSelfRel()
                        .toMono().toFuture().join())
    }

    override fun toCollectionModel(customers: MutableIterable<Customer>): CollectionModel<CustomerModel> {
        return super.toCollectionModel(customers)
    }

    override fun instantiateModel(customer: Customer): CustomerModel {
        return CustomerModel(customer)
    }
}