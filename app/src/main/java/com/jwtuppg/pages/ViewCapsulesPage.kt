package com.jwtuppg.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jwtuppg.messages.Capsule
import com.jwtuppg.security.AuthState
import com.jwtuppg.security.AuthViewModel
import com.jwtuppg.security.DatabaseManager
import com.jwtuppg.security.getUserIdAndEncryptionKeyFromToken

@Composable
fun ViewCapsulesPage(navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()

    // Håll en lista över kapslar
    val capsules = remember { mutableStateListOf<Capsule>() }

    // Kör detta när `authState` förändras
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            is AuthState.Authenticated -> {
                val (userId, _) = getUserIdAndEncryptionKeyFromToken(authState.value)

                // Kontrollera att användar-ID är giltigt
                if (userId > 0) {
                    // Hämta användarens kapslar och uppdatera listan
                    val userCapsules = DatabaseManager.getCapsulesByUserId(userId)
                    capsules.clear()
                    capsules.addAll(userCapsules)
                }
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Your Capsules", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Visa varje kapsel
        capsules.forEach { capsule ->
            Text(text = capsule.message, modifier = Modifier.padding(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("create_capsule") }) {
            Text(text = "Create New Capsule")
        }
    }
}
