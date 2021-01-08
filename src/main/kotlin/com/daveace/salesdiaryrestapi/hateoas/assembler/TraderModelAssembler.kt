package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.CreditController
import com.daveace.salesdiaryrestapi.controller.TraderController
import com.daveace.salesdiaryrestapi.controller.UserController
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.hateoas.model.TraderModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo

class TraderModelAssembler : RepresentationModelAssemblerSupport<Trader, TraderModel>
    (TraderController::class.java, TraderModel::class.java) {

    override fun toModel(trader: Trader): TraderModel {
        return instantiateModel(trader).apply {
            linkTo(WebFluxLinkBuilder.methodOn(CreditController::class.java).findCredit(trader.email))
                .withSelfRel()
                .toMono()
                .subscribe { add(it) }
        }
    }

    override fun toCollectionModel(traders: MutableIterable<Trader>): CollectionModel<TraderModel> {
        return super.toCollectionModel(traders)
    }

    override fun instantiateModel(trader: Trader): TraderModel {
        return TraderModel(trader)
    }
}