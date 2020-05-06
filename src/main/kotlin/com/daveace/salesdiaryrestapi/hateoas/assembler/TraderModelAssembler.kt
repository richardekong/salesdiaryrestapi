package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.TraderController
import com.daveace.salesdiaryrestapi.domain.Trader
import com.daveace.salesdiaryrestapi.hateoas.model.TraderModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport

class TraderModelAssembler : RepresentationModelAssemblerSupport<Trader, TraderModel>
(TraderController::class.java, TraderModel::class.java) {

    override fun toModel(entity: Trader): TraderModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toCollectionModel(traders: MutableIterable<Trader>): CollectionModel<TraderModel> {
        return super.toCollectionModel(traders)
    }

    override fun instantiateModel(trader: Trader): TraderModel {
        return TraderModel(trader)
    }
}