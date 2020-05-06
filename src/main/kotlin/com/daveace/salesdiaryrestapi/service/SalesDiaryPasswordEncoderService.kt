package com.daveace.salesdiaryrestapi.service

import org.springframework.security.crypto.password.PasswordEncoder

interface SalesDiaryPasswordEncoderService{
    fun encode(plainTextPassword: CharSequence?): String
    fun matches(plainTextPassword: CharSequence?, hashedPassword: String?): Boolean
}