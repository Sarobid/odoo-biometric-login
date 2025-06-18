package com.example.viseopos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink // <-- IMPORT THIS
import androidx.navigation.navigation
import com.example.viseopos.ui.screen.FacialRecognitionScreen
import com.example.viseopos.ui.screen.HomeScreen
import com.example.viseopos.ui.screen.WebOdooScreen


object AppDestinations {
    const val HOME_GRAPH_ROUTE = "home_graph"
    const val HOME_SCREEN_ROUTE = "home_screen"
    const val FACIAL_RECOGNITION_ROUTE = "facial_recognition"
    const val FACIAL_RECOGNITION_GRAPH_ROUTE = "facial_recognition_graph"
    const val WEB_ODOO_ROUTE = "web_odoo"
    const val WEB_ODOO_GRAPH_ROUTE = "web_odoo_graph"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController(), modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = AppDestinations.HOME_GRAPH_ROUTE) {
        navigation(
            startDestination = AppDestinations.HOME_SCREEN_ROUTE,
            route = AppDestinations.HOME_GRAPH_ROUTE
        ) {
            composable(AppDestinations.HOME_SCREEN_ROUTE) {
                HomeScreen(navController = navController, modifier = modifier)
            }
        }
        navigation (
            startDestination = AppDestinations.FACIAL_RECOGNITION_ROUTE,
            route = AppDestinations.FACIAL_RECOGNITION_GRAPH_ROUTE
        ){
            composable(
                route = AppDestinations.FACIAL_RECOGNITION_ROUTE
            ) {
                FacialRecognitionScreen(navController = navController)
            }
        }
        navigation(
            startDestination = AppDestinations.WEB_ODOO_ROUTE,
            route = AppDestinations.WEB_ODOO_GRAPH_ROUTE
        ) {
            composable(
                route = AppDestinations.WEB_ODOO_ROUTE
            ) {
                WebOdooScreen(navController = navController, modifier = modifier)
            }
        }
    }
}