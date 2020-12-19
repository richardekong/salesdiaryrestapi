package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.CreditController
import com.daveace.salesdiaryrestapi.domain.Credit
import com.daveace.salesdiaryrestapi.hateoas.model.CreditModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn

class CreditAssembler : RepresentationModelAssemblerSupport<Credit, CreditModel>(CreditController::class.java, CreditModel::class.java) {

    override fun toModel(credit: Credit): CreditModel {
        return instantiateModel(credit)
                .add(linkTo(methodOn(CreditController::class.java)
                        .findCredit(credit.id))
                        .withSelfRel()
                        .toMono()
                        .toFuture()
                        .join())
    }

    override fun toCollectionModel(entities: MutableIterable<Credit>): CollectionModel<CreditModel> {
        return super.toCollectionModel(entities)
    }

    override fun instantiateModel(credit: Credit): CreditModel {
        return CreditModel(credit)
    }
}