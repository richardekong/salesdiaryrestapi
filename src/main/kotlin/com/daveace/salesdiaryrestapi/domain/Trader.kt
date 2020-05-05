package com.daveace.salesdiaryrestapi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Document
data class Trader(
        @Id
        @field:Email(message = EMAIL_VAL_MSG)
        var email: String ="",
        @field:NotNull(message = NAME_VAL_MSG)
        @field:Size(min = MIN_NAME_SIZE)
        var name: String = "",
        @field:NotNull(message = ADDRESS_VAL_MSG)
        @field:Size(min = MIN_ADDRESS_SIZE)
        var address: String =""
){
        companion object{
                val ROLES = arrayOf("USER","TRADER")
        }

        @field:NotNull
        var location:DoubleArray = DoubleArray(2)
        constructor():this(email="")
        constructor(email:String, name:String, address: String, location:DoubleArray):this(){
                this.email = email
                this.name = name
                this.address = address
                this.location[0] = location[0]
                this.location[1] = location[1]
        }
}
