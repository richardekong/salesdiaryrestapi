package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.ExpenditureController
import com.daveace.salesdiaryrestapi.domain.Expenditure
import com.daveace.salesdiaryrestapi.hateoas.model.ExpenditureModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn

class ExpenditureModelAssembler : RepresentationModelAssemblerSupport<Expenditure, ExpenditureModel>
    (ExpenditureController::class.java, ExpenditureModel::class.java) {

    override fun toModel(expenditure: Expenditure): ExpenditureModel {
        return instantiateModel(expenditure)
            .add(
                linkTo(
                    methodOn(ExpenditureController::class.java)
                        .findExpenditureById(expenditure.id)
                )
                    .withSelfRel()
                    .toMono().toFuture().join()
            )
    }

    override fun toCollectionModel(expenditures: MutableIterable<Expenditure>): CollectionModel<ExpenditureModel> {
        return super.toCollectionModel(expenditures)
    }

    override fun instantiateModel(expenditure: Expenditure): ExpenditureModel {
        return ExpenditureModel(expenditure)
    }
}