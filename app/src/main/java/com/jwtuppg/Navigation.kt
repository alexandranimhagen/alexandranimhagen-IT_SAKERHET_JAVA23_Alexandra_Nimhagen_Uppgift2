package com.jwtuppg

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import com.jwtuppg.pages.HomePage
import com.jwtuppg.pages.LoginPage
import com.jwtuppg.pages.SignupPage
import com.jwtuppg.pages.CreateCapsulePage
import com.jwtuppg.pages.ViewCapsulesPage
import com.jwtuppg.security.AuthViewModel

@Composable
fun Navigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home") {
            HomePage(modifier, navController, authViewModel)
        }
        composable("create_capsule") {
            CreateCapsulePage(navController, authViewModel)
        }
        composable("view_capsules") {
            ViewCapsulesPage(navController, authViewModel)
        }
    }
}
