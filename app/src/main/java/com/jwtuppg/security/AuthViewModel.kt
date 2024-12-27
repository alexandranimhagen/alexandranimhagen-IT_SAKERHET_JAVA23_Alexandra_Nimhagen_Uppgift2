package com.jwtuppg.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*

class AuthViewModel : ViewModel() {
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        _authState.value = AuthState.Unauthenticated
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password can't be empty")
            return
        }

        _authState.value = AuthState.Loading

        // Run the login logic in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the stored hashed password from the database
                val storedHashedPassword = DatabaseManager.getUserByEmail(email)

                // Check the password using PasswordUtil
                if (storedHashedPassword != null && PasswordUtil.checkPassword(password, storedHashedPassword)) {
                    val token = JwtUtil.generateToken(email)

                    if (JwtUtil.validateToken(token)) {
                        // Get the user ID and encryption key
                        val userId = DatabaseManager.getUserIdByEmail(email) ?: return@launch
                        val aesKey = DatabaseManager.getEncryptionKey(userId)

                        withContext(Dispatchers.Main) {
                            if (aesKey != null) {
                                _authState.value = AuthState.Authenticated(token, aesKey)
                            } else {
                                _authState.value = AuthState.Error("Failed to retrieve encryption key")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _authState.value = AuthState.Error("Invalid token")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Error("Invalid email or password")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _authState.value = AuthState.Error("An error occurred during login: ${e.message}")
                }
            }
        }
    }

    fun signup(email: String, password: String) {
        // Check for empty inputs
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password can't be empty")
            return
        }

        // Set the state to loading
        _authState.value = AuthState.Loading

        // Run database operations in a coroutine (background thread)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Hash the password before saving
                val hashedPassword = PasswordUtil.hashPassword(password)

                // Save the user with the hashed password
                val userSaved = DatabaseManager.saveUser(email, hashedPassword)

                if (userSaved) {
                    // Generate a new AES key for encryption
                    val aesKey = EncryptionUtil.generateNewKey()

                    // Get the user ID after saving the user
                    val userId = DatabaseManager.getUserIdByEmail(email)

                    if (userId != null && DatabaseManager.saveEncryptionKey(userId, aesKey)) {
                        // Generate JWT token for the user
                        val token = JwtUtil.generateToken(email)

                        // Switch back to the Main thread to update UI state
                        withContext(Dispatchers.Main) {
                            _authState.value = AuthState.Authenticated(token, aesKey)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _authState.value = AuthState.Error("Failed to register user or save encryption key")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _authState.value = AuthState.Error("Failed to register user")
                    }
                }
            } catch (e: Exception) {
                // Handle any unexpected exceptions
                withContext(Dispatchers.Main) {
                    _authState.value = AuthState.Error("An error occurred during registration: ${e.message}")
                }
            }
        }
    }

    fun signout() {
        _authState.value = AuthState.Unauthenticated
    }
}
