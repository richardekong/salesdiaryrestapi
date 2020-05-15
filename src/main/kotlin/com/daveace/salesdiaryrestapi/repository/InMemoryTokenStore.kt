package com.daveace.salesdiaryrestapi.repository

import com.daveace.salesdiaryrestapi.authentication.TokenUtil

class InMemoryTokenStore {
    companion object {

        private val tokenStore: MutableMap<String, String> = mutableMapOf()
        private val tokenUtil: TokenUtil = TokenUtil()

        fun storeToken(token: String) {
            val email: String = tokenUtil.getEmailFromToken(token)
            tokenStore.putIfAbsent(email, token)
        }

        fun storeToken(email:String, token:String){
            tokenStore.putIfAbsent(email, token)
        }

        private fun findToken(email: String): String? {
            return tokenStore[email]
        }

        fun revokeToken(email: String) {
            tokenStore.remove(email)
        }

        fun isRevoked(token: String): Boolean {
            return findToken(tokenUtil.getEmailFromToken(token)) != token
        }
    }
}