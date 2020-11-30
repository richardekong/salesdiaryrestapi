package com.daveace.salesdiaryrestapi.authentication

import com.daveace.salesdiaryrestapi.domain.User
import com.daveace.salesdiaryrestapi.repository.InMemoryTokenStore
import com.daveace.salesdiaryrestapi.repository.ReactiveUserRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
@PropertySource("classpath:jwt.properties")
class TokenUtil {

    companion object {
        const val TOKEN_VALIDITY = 1800000L
        const val SECRET = "\${jwt.secret}"
    }

    @Autowired
    private lateinit var userRepo: ReactiveUserRepository

    fun getIdFromToken(token: String):String{
        return getAllClaimsFromToken(token)["id"].toString()
    }

    fun getEmailFromToken(token: String): String {
        return getAllClaimsFromToken(token).subject
    }

    fun getUserFromToken(token: String): User {
        return userRepo.findUserByEmail(getEmailFromToken(token)).toFuture().join()
    }

    fun getExpirationDateFromToken(token: String): Date {
        return getAllClaimsFromToken(token).expiration
    }

    fun generateToken(user: User, validity: Long = TOKEN_VALIDITY): String {
        val claims: MutableMap<String, Any> = mutableMapOf()
        claims["id"] = user.id
        val token:String= doGenerateToken(claims, user.email, validity)
        InMemoryTokenStore.storeToken(user.email, token)
        return token
    }

    fun getAllClaimsFromToken(token: String): Claims {
        return Jwts
                .parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .body
    }

    private fun doGenerateToken(claims: MutableMap<String, Any>, subject: String, validity: Long = TOKEN_VALIDITY): String {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + validity))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact()
    }

    fun isTokenExpired(token: String): Boolean {
        val expiration: Date = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    fun isTokenRevoked(token: String):Boolean{
        return InMemoryTokenStore.isRevoked(token)
    }

}