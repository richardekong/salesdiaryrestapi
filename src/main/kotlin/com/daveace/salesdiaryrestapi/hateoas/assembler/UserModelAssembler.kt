package com.daveace.salesdiaryrestapi.hateoas.assembler

import com.daveace.salesdiaryrestapi.controller.CreditController
import com.daveace.salesdiaryrestapi.controller.UserController
import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.hateoas.model.UserModel
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn

class UserModelAssembler :
    RepresentationModelAssemblerSupport<User, UserModel>(UserController::class.java, UserModel::class.java) {
    override fun toModel(user: User): UserModel {
        return instantiateModel(user).apply {
            linkTo(methodOn(CreditController::class.java).findCredit(user.email))
                .withSelfRel()
                .toMono()
                .subscribe { add(it) }
        }
    }

    override fun toCollectionModel(users: MutableIterable<User>): CollectionModel<UserModel> {
        return super.toCollectionModel(users)
    }

    override fun instantiateModel(user: User): UserModel {
        return UserModel(user)
    }
}
