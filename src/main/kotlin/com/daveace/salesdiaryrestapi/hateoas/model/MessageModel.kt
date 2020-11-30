package com.daveace.salesdiaryrestapi.hateoas.model

import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(value = "message", collectionRelation = "messages")
data class MessageModel(val message:String): RepresentationModel<MessageModel>()