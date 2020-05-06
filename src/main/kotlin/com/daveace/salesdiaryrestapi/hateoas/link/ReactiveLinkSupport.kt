package com.daveace.salesdiaryrestapi.hateoas.link

import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveLinkSupport {

    fun <S : RepresentationModel<S>, T : Any> respondWithReactiveLink(
            model: S,
            method: T
    ): Mono<S> = linkTo(method).withSelfRel().toMono().map { model.add(it) }


    fun <R, S : RepresentationModel<S>, T : Any> respondWithReactiveLinks(
            assemblerSupport: RepresentationModelAssemblerSupport<R, S>,
            resourceFlux: Flux<R>,
            invokedMethod: T,
            relation: String
    ): Flux<CollectionModel<S>> = resourceFlux.collectList().map { entities ->
        Flux.just(assemblerSupport.toCollectionModel(entities))
                .flatMap { models ->
                    linkTo(invokedMethod).withRel(relation)
                            .toMono().map(models::add)
                }
    }.toFuture().join()


}