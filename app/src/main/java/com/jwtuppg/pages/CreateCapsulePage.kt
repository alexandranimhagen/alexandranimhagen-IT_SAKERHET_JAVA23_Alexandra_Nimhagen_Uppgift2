package com.jwtuppg.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jwtuppg.security.AuthViewModel
import com.jwtuppg.security.DatabaseManager
import com.jwtuppg.security.EncryptionUtil
import com.jwtuppg.security.getUserIdAndEncryptionKeyFromToken
import kotlinx.coroutines.launch

@Composable
fun CreateCapsulePage(navController: NavController, authViewModel: AuthViewModel) {
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() // Skapa en CoroutineScope

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create Capsule")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(text = "Message") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (message.isNotEmpty()) {
                coroutineScope.launch {
                    // Hämta användarens ID och krypteringsnyckel
                    val (userId, encryptionKey) = getUserIdAndEncryptionKeyFromToken(authViewModel.authState.value)

                    // Kontrollera om krypteringsnyckeln existerar
                    if (encryptionKey != null) {
                        // Kryptera meddelandet med användarens AES-nyckel
                        val encryptedMessage = EncryptionUtil.encrypt(message, encryptionKey)

                        // Spara tidskapseln i databasen med aktuellt timestamp
                        val success = DatabaseManager.saveCapsule(userId, encryptedMessage, System.currentTimeMillis())

                        // Hantera resultatet i huvudtråden
                        if (success) {
                            Toast.makeText(context, "Capsule saved!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home")
                        } else {
                            Toast.makeText(context, "Failed to save capsule.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Failed to retrieve encryption key.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }) {
            Text(text = "Save Capsule")
        }
    }
}
