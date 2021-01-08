package com.daveace.salesdiaryrestapi.hateoas.model

import com.daveace.salesdiaryrestapi.domain.Credit
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDate

@Relation(value = "Credit", collectionRelation = "Credits")
class CreditModel() : RepresentationModel<CreditModel>() {

    var id: String = ""
    var traderId :String = ""
    var customerId: String = ""
    var productId: String = ""
    var product:String = ""
    var salesPrice: Double = 0.0
    var quantity: Double = 0.0
    lateinit var date: LocalDate
    var redeemed: Boolean = false

    constructor(credit: Credit):this() {
        id = credit.id
        traderId = credit.traderId()
        customerId = credit.customerId()
        productId = credit.productId()
        product = credit.product()
        salesPrice = credit.salesPrice()
        quantity = credit.quantity()
        date = credit.date()
        redeemed = credit.redeemed()
    }


}

