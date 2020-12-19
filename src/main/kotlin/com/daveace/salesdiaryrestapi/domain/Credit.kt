package com.daveace.salesdiaryrestapi.domain

import com.daveace.salesdiaryrestapi.mapper.Mappable
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document
data class Credit(@field:Id val id: String = SalesDiaryId.generateId()) : Mappable {

    private var traderId: String = ""
    private var customerId: String = ""
    private var productId: String = ""
    private var product: String = ""
    private var costPrice: Double = 0.0
    private var salesPrice: Double = 0.0
    private var quantity: Double = 0.0
    private var left: Double = 0.0
    private var date: LocalDate = LocalDate.now()
    private var location = MutableList(2) { it * 0.0 }
    private var redeemed: Boolean = false

    constructor(event: SalesEvent) : this() {
        traderId = event.traderId
        customerId = event.customerId
        productId = event.productId
        product = event.product
        costPrice = event.costPrice
        salesPrice = event.salesPrice.times(-1)
        quantity = event.quantitySold
        left = event.left
        date = event.date
        location[0] = event.location[0]
        location[1] = event.location[1]
    }

    fun redeem(value: Boolean) {
        redeemed = value
    }

    fun traderId() = traderId
    fun customerId() = customerId
    fun productId() = productId
    fun product() = product
    fun costPrice() = costPrice
    fun salesPrice() = salesPrice
    fun quantity() = quantity
    fun left() = left
    fun date() = date
    fun location() = location
    fun redeemed() = redeemed


}