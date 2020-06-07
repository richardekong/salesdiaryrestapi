package com.daveace.salesdiaryrestapi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Document
data class User(
        @Id
        val id:String = UUID.randomUUID().toString(),
        @field:Email(message = EMAIL_VAL_MSG)
        var email: String = "",
        @field:Size(min = MIN_PASSWORD_SIZE, message = PASSWORD_SIZE_VAL_MSG)
        var userPassword: String = ""
) : UserDetails {
    var trader: Trader? = null


    companion object {
        const val ROLE: String = "USER"
    }

    constructor(email:String,password:String):this(){
        this.email = email
        this.userPassword = password
    }

    override fun getAuthorities(): List<GrantedAuthority> = listOf()
    override fun isEnabled(): Boolean = true
    override fun getUsername(): String = email
    override fun isCredentialsNonExpired(): Boolean = true
    override fun getPassword() = userPassword
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true

}
