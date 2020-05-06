package com.daveace.salesdiaryrestapi.service

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class SalesDiaryPasswordEncoderServiceImpl : SalesDiaryPasswordEncoderService {

    private val passwordEncoder : BCryptPasswordEncoder = BCryptPasswordEncoder()

    override fun encode(plainTextPassword: CharSequence?): String {
        return passwordEncoder.encode(plainTextPassword)
    }

    override fun matches(plainTextPassword: CharSequence?, hashedPassword: String?): Boolean {
        return passwordEncoder.matches(plainTextPassword, hashedPassword)
    }
}