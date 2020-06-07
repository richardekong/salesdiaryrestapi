package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.User
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(value = "user", collectionRelation = "users")
class UserModel() : RepresentationModel<UserModel>() {

    lateinit var email: String
    lateinit var password: String

    constructor(user:User):this(){
        this.email = user.email
        this.password = user.password
    }

}

