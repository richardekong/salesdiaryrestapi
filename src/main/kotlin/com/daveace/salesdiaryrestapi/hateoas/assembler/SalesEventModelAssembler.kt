package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.SalesEventController
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.hateoas.model.SalesEventModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport

class SalesEventModelAssembler: RepresentationModelAssemblerSupport<SalesEvent, SalesEventModel>(
        SalesEventController::class.java,
        SalesEventModel::class.java
) {

    override fun toModel(event: SalesEvent): SalesEventModel {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toCollectionModel(events: MutableIterable<SalesEvent>): CollectionModel<SalesEventModel> {
        return super.toCollectionModel(events)
    }

    override fun instantiateModel(event: SalesEvent): SalesEventModel {
        return SalesEventModel(event)
    }
}