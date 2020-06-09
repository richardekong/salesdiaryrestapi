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
        @Id
        val id: String = UUID.randomUUID().toString(),
        var traderId: String,
        var productId: String,
        var customerId: String,
        @field:DecimalMin(value = "0.00")
        val quantitySold: Double,
        @field:DecimalMin(value = "0.00")
        val costPrice: Double,
        @field:DecimalMin(value = "0.01")
        val salesPrice: Double,
        @field:DecimalMin(value = "0.00")
        val left: Double,
        val date: Date = Date(),
        @field:NotNull
        val location: Point = Point(0.0, 0.0))