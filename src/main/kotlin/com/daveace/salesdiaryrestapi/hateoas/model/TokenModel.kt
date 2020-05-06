package com.daveace.salesdiaryrestapi.hateoas.model

import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation


@Relation(value = "token", collectionRelation = "tokens")
data class TokenModel(val token: String) : RepresentationModel<TokenModel>()