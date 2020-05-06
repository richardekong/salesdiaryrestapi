package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.CustomerController
import com.daveace.salesdiaryrestapi.domain.Customer
import com.daveace.salesdiaryrestapi.hateoas.model.CustomerModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport

class CustomerModelAssembler : RepresentationModelAssemblerSupport<Customer, CustomerModel>
(CustomerController::class.java, CustomerModel::class.java) {

    override fun toModel(customer: Customer): CustomerModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toCollectionModel(customers: MutableIterable<Customer>): CollectionModel<CustomerModel> {
        return super.toCollectionModel(customers)
    }

    override fun instantiateModel(customer: Customer): CustomerModel {
        return super.instantiateModel(customer)
    }
}