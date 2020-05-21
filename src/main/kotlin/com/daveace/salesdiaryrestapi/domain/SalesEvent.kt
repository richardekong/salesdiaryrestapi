package com.daveace.salesdiaryrestapi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import javax.persistence.*
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotNull


@Document
data class SalesEvent(
        var traderId: String,
        var productId: String,
        var customerId:String,
        @field:DecimalMin(value = "0.00")
        val sales: Double,
        @field:DecimalMin(value = "0.00")
        val left: Double,
        @field:DecimalMin(value = "0.01")
        val price: Double,
        @field:NotNull
        val location: Point = Point(0.0, 0.0)
) {
    @Id
    val id: String = UUID.randomUUID().toString()
    val date: Date = Date()
}