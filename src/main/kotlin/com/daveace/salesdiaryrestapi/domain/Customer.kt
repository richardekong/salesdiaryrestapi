package com.daveace.salesdiaryrestapi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Document
data class Customer(
        @Id
        @field:Email(message = EMAIL_VAL_MSG)
        var email: String = "",
        @field:NotNull(message = NAME_VAL_MSG)
        @field:Size(min = MIN_NAME_SIZE, message = MIN_NAME_SIZE_VAL_MSG)
        var name: String = "",
        var traderId: String = "",
        @field:NotBlank(message = COMPANY_NAME_VAL_MSG)
        var company: String = "",
        @Field(name = "signature_path")
        var signaturePath: String = "",
        @field:NotBlank(message = ADDRESS_VAL_MSG)
        var address: String = ""
) {

    companion object{
        val ROLES = arrayOf("USER","CUSTOMER")
    }

    @field:NotNull
    var location: DoubleArray = DoubleArray(2)

    constructor() : this(email = "")
    constructor(
            email: String, name: String, company: String,
            address: String, location: DoubleArray
    ) : this() {
        this.email = email
        this.name = name
        this.company = company
        this.address = address
        this.location[0] = location[0]
        this.location[1] = location[1]
    }
}