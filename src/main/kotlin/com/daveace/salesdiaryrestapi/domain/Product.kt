package com.daveace.salesdiaryrestapi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import javax.validation.constraints.*

@Document
data class Product(
        @field:NotBlank(message = PRODUCT_NAME_VAL_MSG)
        @field:Size(min = PRODUCT_NAME_SIZE, message = PRODUCT_NAME_SIZE_VAL_MSG)
        var name: String
) {

    @Id
    val id: String = UUID.randomUUID().toString()
    lateinit var traderId: String
    val date: Date = Date()
    @field:NotNull(message = PRODUCT_CODE_VAL_MSG)
    lateinit var code: String
    lateinit var imagePath: String
    @field:DecimalMin("0.0")
    var stock: Double = 0.0
        set(value) {
            if (value >= 0) field = value
        }
    @field:DecimalMin("1.0")
    var cost: Double = 0.0
        set(value) {
            if (value >= 0) field = value
        }

}

