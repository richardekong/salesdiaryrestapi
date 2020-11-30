package com.daveace.salesdiaryrestapi.domain

data class GmailCredentials(
        val appEmail: String,
        val clientId: String,
        val clientSecret: String,
        val accessToken: String,
        val refreshToken: String
)