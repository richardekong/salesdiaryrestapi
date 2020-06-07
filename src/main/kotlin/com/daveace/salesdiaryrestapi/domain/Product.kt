package com.daveace.salesdiaryrestapi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import javax.validation.constraints.*

@Document
data class Product(
        @Id
        val id: String = UUID.randomUUID().toString(),
        var traderId: String = "",
        @field:NotBlank(message = PRODUCT_NAME_VAL_MSG)
        @field:Size(min = PRODUCT_NAME_SIZE, message = PRODUCT_NAME_SIZE_VAL_MSG)
        var name: String = "",
        @field:NotBlank(message = PRODUCT_CODE_VAL_MSG)
        var code: String = "",
        var imagePath: String = "",
        val date: Date = Date) {

    @field:DecimalMin("0.0")
    var stock: Double = 0.0
        set(value) {
            if (value >= 0.0) field = value
        }

    @field:DecimalMin("1.0")
    var cost: Double = 0.0
        set(value) {
            if (value >= 0.0) field = value
        }

    constructor(traderId: String, name: String, code: String, imagePath: String, stock: Double, cost: Double) : this() {
        this.traderId = traderId
        this.name = name
        this.code = code
        this.imagePath = imagePath
        this.stock = stock
        this.cost = cost
    }

}

