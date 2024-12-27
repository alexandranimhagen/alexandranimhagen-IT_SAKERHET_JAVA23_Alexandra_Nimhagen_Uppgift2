package com.jwtuppg.security

import org.mindrot.jbcrypt.BCrypt

object PasswordUtil {

    // Function to hash a plain password
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    // Function to check a plain password against a hashed password
    fun checkPassword(plainTextPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainTextPassword, hashedPassword)
    }
}
