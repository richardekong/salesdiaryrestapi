package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.User
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation

@Relation(value = "user", collectionRelation = "users")
data class UserModel(@JsonIgnore val user: User) : RepresentationModel<UserModel>() {
    val email: String = user.email
    val password: String = user.password
    val phone: String = user.phone
    val kind:String = determineKind()

    private fun determineKind():String{
        return when{
            user.customer!= null -> user.customer!!::class.java.simpleName
            user.trader!=null -> user.trader!!::class.java.simpleName
            else -> user.kind
        }
    }

}

