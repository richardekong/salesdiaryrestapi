package com.daveace.salesdiaryrestapi.authentication

import com.daveace.salesdiaryrestapi.domain.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component
import java.util.*

@Component
@PropertySource("classpath:jwt.properties")
class TokenUtil {

    @Value("\${jwt.secret}")
    private lateinit var secret:String

    companion object {
        const val TOKEN_VALIDITY = 1800000L
    }

    fun getEmailFromToken(token: String): String {
        return getAllClaimsFromToken(token).subject
    }

    fun getExpirationDateFromToken(token: String): Date {
        return getAllClaimsFromToken(token).expiration
    }

    fun generateToken(user: User, validity:Long = TOKEN_VALIDITY): String {
        val claims: MutableMap<String, Any> = mutableMapOf()
        return doGenerateToken(claims, user.email, validity)
    }

    fun getAllClaimsFromToken(token: String): Claims {
        return Jwts
                .parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .body
    }

    private fun doGenerateToken(claims: MutableMap<String, Any>, subject: String, validity: Long = TOKEN_VALIDITY): String {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + validity))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact()
    }

    fun isTokenExpired(token:String):Boolean{
        val expiration:Date= getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    fun validateToken(token:String, usr:User):Boolean{
        val email:String = getEmailFromToken(token)
        return email == usr.email &&  !isTokenExpired(token)
    }

}