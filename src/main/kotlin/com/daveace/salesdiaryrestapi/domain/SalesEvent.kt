package com.daveace.salesdiaryrestapi.domain

import com.daveace.salesdiaryrestapi.mapper.Mappable
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotNull


@Document
data class SalesEvent(
        @Id
        val id: String = SalesDiaryId.generateId(),
        var traderId: String = "",
        var productId: String = "",
        var customerId: String = "",
        var product:String="",
        @field:DecimalMin(value = "0.00")
        var quantitySold: Double = 0.0,
        @field:DecimalMin(value = "0.00")
        var costPrice: Double = 0.0,
        @field:DecimalMin(value = "0.01")
        var salesPrice: Double = 0.0,
        @field:DecimalMin(value = "0.00")
        var left: Double = 0.0,
        val date: LocalDate = LocalDate.now(),
        @field:NotNull
        val location: MutableList<Double> = MutableList(2) { it * 0.0 }):Mappable {

    constructor(traderId: String, customerId: String, productId:String, product:String="", quantitySold: Double, costPrice: Double,
                salesPrice: Double, left: Double, location: MutableList<Double>) : this() {
        this.traderId = traderId
        this.customerId = customerId
        this.productId = productId
        this.product = product
        this.quantitySold = quantitySold
        this.costPrice = costPrice
        this.salesPrice = salesPrice
        this.left = left
        this.location[0] = location[0]
        this.location[1] = location[1]
    }

}