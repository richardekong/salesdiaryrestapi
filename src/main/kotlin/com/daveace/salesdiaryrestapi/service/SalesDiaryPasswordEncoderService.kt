package com.daveace.salesdiaryrestapi.service

interface SalesDiaryPasswordEncoderService{
    fun encode(plainTextPassword: CharSequence?): String
    fun matches(plainTextPassword: CharSequence?, hashedPassword: String?): Boolean
}