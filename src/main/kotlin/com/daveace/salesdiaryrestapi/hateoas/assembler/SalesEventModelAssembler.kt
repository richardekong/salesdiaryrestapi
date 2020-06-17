package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.SalesEventController
import com.daveace.salesdiaryrestapi.controller.UserController
import com.daveace.salesdiaryrestapi.domain.SalesEvent
import com.daveace.salesdiaryrestapi.hateoas.model.SalesEventModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo

class SalesEventModelAssembler: RepresentationModelAssemblerSupport<SalesEvent, SalesEventModel>(
        SalesEventController::class.java,
        SalesEventModel::class.java
) {

    override fun toModel(event: SalesEvent): SalesEventModel {
        return instantiateModel(event)
                .add(linkTo(WebFluxLinkBuilder.methodOn(SalesEventController::class.java)
                        .findSalesEventById(event.id))
                        .withSelfRel()
                        .toMono().toFuture().join())
    }

    override fun toCollectionModel(events: MutableIterable<SalesEvent>): CollectionModel<SalesEventModel> {
        return super.toCollectionModel(events)
    }

    override fun instantiateModel(event: SalesEvent): SalesEventModel {
        return SalesEventModel(event)
    }
}