package com.jwtuppg.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jwtuppg.security.AuthViewModel
import com.jwtuppg.messages.Message
import com.jwtuppg.security.AuthState
import com.jwtuppg.security.DatabaseManager
import com.jwtuppg.security.JwtUtil
import com.jwtuppg.security.EncryptionUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    val messages = remember { mutableStateListOf<Message>() }
    val authState by authViewModel.authState.observeAsState()
    val listState = rememberLazyListState()

    // Lyssna på autentiseringsstatus och hämta meddelanden
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val jwtToken = (authState as AuthState.Authenticated).token
            fetchMessages(messages, jwtToken)  // Anropa funktionen här
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Page", fontSize = 32.sp)

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            items(messages.size) { index ->
                val message = messages[index]
                Text(
                    text = "${message.user}: ${message.content}",
                    modifier = Modifier.padding(8.dp),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        var messageText by remember { mutableStateOf("") }

        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = { Text("Write a message") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            val currentUser = JwtUtil.getEmailFromToken((authState as? AuthState.Authenticated)?.token ?: return@Button)

            // Kör en coroutine när knappen klickas
            // Eftersom Button är en @Composable-funktion, kan vi inte direkt använda LaunchedEffect, utan måste använda en separat coroutine-scope
            CoroutineScope(Dispatchers.IO).launch {
                val userId = DatabaseManager.getUserIdByEmail(currentUser) ?: return@launch
                val encryptionKeyString = DatabaseManager.getEncryptionKey(userId) ?: return@launch

                // Kryptera meddelandet och spara det
                val encryptedMessage = EncryptionUtil.encrypt(messageText, encryptionKeyString)
                val isSaved = DatabaseManager.saveCapsule(userId, encryptedMessage, System.currentTimeMillis())

                withContext(Dispatchers.Main) {
                    if (isSaved) {
                        messageText = ""
                    }
                }
            }
        }) {
            Text(text = "Send")
        }

        TextButton(onClick = {
            authViewModel.signout()
            navController.navigate("login")
        }) {
            Text(text = "Sign Out")
        }
    }
}

suspend fun fetchMessages(messages: SnapshotStateList<Message>, jwtToken: String?) {
    val email = JwtUtil.getEmailFromToken(jwtToken ?: return)
    val userId = DatabaseManager.getUserIdByEmail(email) ?: return

    // Hämta krypteringsnyckeln och meddelanden
    val encryptionKeyString = DatabaseManager.getEncryptionKey(userId) ?: return
    val encryptionKey = EncryptionUtil.stringToKey(encryptionKeyString)
    val fetchedCapsules = DatabaseManager.getCapsulesByUserId(userId)

    withContext(Dispatchers.Main) {
        messages.clear()
        messages.addAll(fetchedCapsules.map { capsule ->
            val decryptedMessage = EncryptionUtil.decrypt(capsule.message, encryptionKey.toString())
            Message(user = email, content = decryptedMessage, timestamp = capsule.timestamp)
        })
    }
}
