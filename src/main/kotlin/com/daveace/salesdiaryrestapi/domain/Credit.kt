package com.daveace.salesdiaryrestapi.domain

import com.daveace.salesdiaryrestapi.mapper.Mappable
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document
data class Credit(@field:Id val id: String = SalesDiaryId.generateId()) : Mappable {

    private var traderId:String = ""
    private var customerId: String = ""
    private var productId: String = ""
    private var salesPrice: Double = 0.0
    private var quantity: Double = 0.0
    private var date: LocalDate = LocalDate.now()
    private var redeemed: Boolean = false

    constructor(event: SalesEvent) : this() {
        traderId = event.traderId
        customerId = event.customerId
        productId = event.productId
        salesPrice = event.salesPrice
        quantity = event.quantitySold
        date = event.date
    }

    fun redeem(value: Boolean) {
        redeemed = value
    }

    fun traderId() = traderId
    fun customerId() = customerId
    fun productId() = productId
    fun salesPrice() = salesPrice
    fun quantity() = quantity
    fun date() = date
    fun redeemed() = redeemed


}