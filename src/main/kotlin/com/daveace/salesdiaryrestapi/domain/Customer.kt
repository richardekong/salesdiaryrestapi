package com.daveace.salesdiaryrestapi.domain

import com.daveace.salesdiaryrestapi.mapper.Mappable
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Document
data class Customer(
    @Id
    val id: String = SalesDiaryId.generateId(),
    @field:Email(message = EMAIL_VAL_MSG)
    var email: String = "",
    @field:NotBlank(message = PHONE_VAL_MSG)
    var phone: String = "",
    @field:NotNull(message = NAME_VAL_MSG)
    @field:Size(min = MIN_NAME_SIZE, message = MIN_NAME_SIZE_VAL_MSG)
    var name: String = "",
    var traderId: String = "",
    @field:NotBlank(message = COMPANY_NAME_VAL_MSG)
    var company: String = "",
    @Field(name = "signature_path")
    var signaturePath: String = "",
    @field:NotBlank(message = ADDRESS_VAL_MSG)
    var address: String = "",
    val date: LocalDate = LocalDate.now()
) : Mappable {

    companion object {
        val ROLES = arrayOf("USER", "CUSTOMER")
    }

    @field:NotNull
    var location: MutableList<Double> = mutableListOf()
    var products: MutableList<Product> = mutableListOf()


    constructor() : this(email = "")
    constructor(
        email: String, name: String, phone: String, traderId: String, company: String,
        address: String, location: MutableList<Double>
    ) : this() {

        this.email = email
        this.name = name
        this.phone = phone
        this.traderId = traderId
        this.company = company
        this.address = address
        this.location = location
    }

}
