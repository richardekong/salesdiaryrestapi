package com.daveace.salesdiaryrestapi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Document
data class User(
        @Id
        @field:Email(message = EMAIL_VAL_MSG)
        val email: String = "",
        @field:Size(min = MIN_PASSWORD_SIZE, message = PASSWORD_SIZE_VAL_MSG)
        var userPassword: String = "",
        @field:NotBlank(message = PHONE_VAL_MSG)
        @field:Size(min = MIN_PHONE_SIZE)
        var phone: String = ""
) : UserDetails {
    var trader: Trader? = null


    companion object {
        const val ROLE: String = "USER"
    }

    override fun getAuthorities(): List<GrantedAuthority> = listOf()
    override fun isEnabled(): Boolean = true
    override fun getUsername(): String = email
    override fun isCredentialsNonExpired(): Boolean = true
    override fun getPassword() = userPassword
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true

}
