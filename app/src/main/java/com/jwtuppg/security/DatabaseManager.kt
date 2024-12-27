package com.jwtuppg.security

import com.jwtuppg.messages.Capsule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
//import org.mindrot.jbcrypt.BCrypt

object DatabaseManager {

    private const val JDBC_URL = "jdbc:mysql://10.0.2.2:3306/tidskapslar_db?useSSL=false"
    private const val USER = "root"
    private const val PASSWORD = "password"

    // Anslut till databasen
    suspend fun connect(): Connection? = withContext(Dispatchers.IO) {
        try {
            DriverManager.getConnection(JDBC_URL, USER, PASSWORD)
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }

    // Stäng anslutning och resurser
    suspend fun close(connection: Connection?, preparedStatement: PreparedStatement?, resultSet: ResultSet?) {
        withContext(Dispatchers.IO) {
            try {
                resultSet?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            try {
                preparedStatement?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            try {
                connection?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveUser(email: String, plainPassword: String): Boolean {
        val connection = connect()
        val query = "INSERT INTO users (email, password) VALUES (?, ?)"
        var preparedStatement: PreparedStatement? = null

        return withContext(Dispatchers.IO) {
            try {
                // Hash the password before saving it
                val hashedPassword = PasswordUtil.hashPassword(plainPassword)

                preparedStatement = connection?.prepareStatement(query)
                preparedStatement?.setString(1, email)
                preparedStatement?.setString(2, hashedPassword)
                val rowsAffected = preparedStatement?.executeUpdate()
                rowsAffected != null && rowsAffected > 0
            } catch (e: SQLException) {
                e.printStackTrace()
                false
            } finally {
                close(connection, preparedStatement, null)
            }
        }
    }


    suspend fun getUserByEmail(email: String): String? = withContext(Dispatchers.IO) {
        val connection = connect()
        val query = "SELECT password FROM users WHERE email = ?"
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        var password: String? = null

        try {
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setString(1, email)
            resultSet = preparedStatement?.executeQuery()

            if (resultSet?.next() == true) {
                password = resultSet.getString("password")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            close(connection, preparedStatement, resultSet)
        }
        password
    }

    // Hämta användar-ID via email
    suspend fun getUserIdByEmail(email: String): Int? {
        val connection = connect()
        val query = "SELECT id FROM users WHERE email = ?"
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        return withContext(Dispatchers.IO) {
            try {
                preparedStatement = connection?.prepareStatement(query)
                preparedStatement?.setString(1, email)
                resultSet = preparedStatement?.executeQuery()

                resultSet?.let { rs ->
                    if (rs.next()) {
                        return@withContext rs.getInt("id")
                    }
                }
                null
            } catch (e: SQLException) {
                e.printStackTrace()
                null
            } finally {
                close(connection, preparedStatement, resultSet)
            }
        }
    }

    // Spara krypterad kapsel
    suspend fun saveCapsule(userId: Int, encryptedMessage: String, timestamp: Long): Boolean {
        val connection = connect()
        val query = "INSERT INTO capsules (user_id, message, timestamp) VALUES (?, ?, ?)"
        var preparedStatement: PreparedStatement? = null

        return withContext(Dispatchers.IO) {
            try {
                preparedStatement = connection?.prepareStatement(query)
                preparedStatement?.setInt(1, userId)
                preparedStatement?.setString(2, encryptedMessage)
                preparedStatement?.setLong(3, timestamp) // Lägg till timestamp
                val rowsAffected = preparedStatement?.executeUpdate()
                rowsAffected != null && rowsAffected > 0
            } catch (e: SQLException) {
                e.printStackTrace()
                false
            } finally {
                close(connection, preparedStatement, null)
            }
        }
    }

    // Hämta kapslar via användar-ID
    suspend fun getCapsulesByUserId(userId: Int): List<Capsule> {
        val connection = connect()
        val query = "SELECT id, message, timestamp FROM capsules WHERE user_id = ?"
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        val capsules = mutableListOf<Capsule>()

        return withContext(Dispatchers.IO) {
            try {
                preparedStatement = connection?.prepareStatement(query)
                preparedStatement?.setInt(1, userId)
                resultSet = preparedStatement?.executeQuery()

                resultSet?.let { rs ->
                    while (rs.next()) {
                        capsules.add(
                            Capsule(
                                rs.getInt("id"),
                                userId,
                                rs.getString("message"),
                                rs.getLong("timestamp")
                            )
                        )
                    }
                }
                capsules
            } catch (e: SQLException) {
                e.printStackTrace()
                capsules
            } finally {
                close(connection, preparedStatement, resultSet)
            }
        }
    }

    // Spara krypteringsnyckel
    suspend fun saveEncryptionKey(userId: Int, aesKey: String): Boolean {
        val connection = connect()
        val query = "INSERT INTO encryption_keys (user_id, aes_key) VALUES (?, ?)"
        var preparedStatement: PreparedStatement? = null

        return withContext(Dispatchers.IO) {
            try {
                preparedStatement = connection?.prepareStatement(query)
                preparedStatement?.setInt(1, userId)
                preparedStatement?.setString(2, aesKey)
                val rowsAffected = preparedStatement?.executeUpdate()
                rowsAffected != null && rowsAffected > 0
            } catch (e: SQLException) {
                e.printStackTrace()
                false
            } finally {
                close(connection, preparedStatement, null)
            }
        }
    }

    // Hämta krypteringsnyckel via användar-ID
    suspend fun getEncryptionKey(userId: Int): String? {
        val connection = connect()
        val query = "SELECT aes_key FROM encryption_keys WHERE user_id = ?"
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        return withContext(Dispatchers.IO) {
            try {
                preparedStatement = connection?.prepareStatement(query)
                preparedStatement?.setInt(1, userId)
                resultSet = preparedStatement?.executeQuery()

                resultSet?.let { rs ->
                    if (rs.next()) {
                        return@withContext rs.getString("aes_key")
                    }
                }
                null
            } catch (e: SQLException) {
                e.printStackTrace()
                null
            } finally {
                close(connection, preparedStatement, resultSet)
            }
        }
    }
}