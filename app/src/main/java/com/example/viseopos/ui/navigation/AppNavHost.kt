package com.example.viseopos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink // <-- IMPORT THIS
import androidx.navigation.navigation
import com.example.viseopos.ui.screen.AuthViaCodeScreen
import com.example.viseopos.ui.screen.FacialRecognitionScreen
import com.example.viseopos.ui.screen.HomeScreen
import com.example.viseopos.ui.screen.SettingScreen
import com.example.viseopos.ui.screen.WebOdooScreen
import com.example.viseopos.utils.WebOdooUtils
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


object AppDestinations {
    const val HOME_GRAPH_ROUTE = "home_graph"
    const val HOME_SCREEN_ROUTE = "home_screen"
    const val FACIAL_RECOGNITION_ROUTE = "facial_recognition"
    const val FACIAL_RECOGNITION_GRAPH_ROUTE = "facial_recognition_graph"
    const val WEB_ODOO_ROUTE = "web_odoo"
    const val WEB_ODOO_GRAPH_ROUTE = "web_odoo_graph"
    const val AUTH_VIA_CODE_ROUTE = "auth_via_code"
    const val AUTH_VIA_CODE_GRAPH_ROUTE = "auth_via_code_graph"
    const val SETTINGS_ROUTE = "settings"
    const val SETTINGS_GRAPH_ROUTE = "settings_graph"
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
                route = AppDestinations.WEB_ODOO_ROUTE + "/{token}/{encodedHostname}/{dbname}",
                arguments = listOf(navArgument("token") { type =
                    NavType.StringType },
                    navArgument("encodedHostname") { type = NavType.StringType },
                    navArgument("dbname") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token")
                val encodedHostnameArg = backStackEntry.arguments?.getString("encodedHostname")
                val hostname = WebOdooUtils.decodeHostname(encodedHostnameArg.toString())
                val dbname = backStackEntry.arguments?.getString("dbname")
                WebOdooScreen(navController = navController, modifier = modifier,token = token.toString(), hostname = hostname.toString(),
                    dbName=dbname.toString())
            }
        }
        navigation(
            startDestination = AppDestinations.AUTH_VIA_CODE_ROUTE,
            route = AppDestinations.AUTH_VIA_CODE_GRAPH_ROUTE
        ) {
            composable(
                route = AppDestinations.AUTH_VIA_CODE_ROUTE
            ) {
                AuthViaCodeScreen(navController = navController,modifier = modifier)
            }
        }
        navigation(
            startDestination = AppDestinations.SETTINGS_ROUTE,
            route = AppDestinations.SETTINGS_GRAPH_ROUTE
        ) {
            composable(AppDestinations.SETTINGS_ROUTE) {
                SettingScreen(navController = navController, modifier = modifier)
            }
        }
    }
}