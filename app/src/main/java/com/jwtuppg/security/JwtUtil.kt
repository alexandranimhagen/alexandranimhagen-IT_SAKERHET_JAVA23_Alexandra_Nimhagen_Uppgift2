@file:Suppress("DEPRECATION")

package com.jwtuppg.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.security.Key
import javax.crypto.spec.SecretKeySpec
import java.util.Date

object JwtUtil {
    private const val SECRET_KEY = "JwtUtilKey"
    private const val EXPIRATION_TIME = 86400000

    fun generateToken(email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + EXPIRATION_TIME)

        val key: Key = SecretKeySpec(SECRET_KEY.toByteArray(), SignatureAlgorithm.HS512.jcaName)

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            !isTokenExpired(claims)
        } catch (e: Exception) {
            false
        }
    }

    fun getEmailFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims.subject
    }

    private fun getClaimsFromToken(token: String): Claims {
        val key: Key = SecretKeySpec(SECRET_KEY.toByteArray(), SignatureAlgorithm.HS512.jcaName)
        return Jwts.parser()
            .setSigningKey(key)
            .parseClaimsJws(token)
            .body
    }

    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }
}
