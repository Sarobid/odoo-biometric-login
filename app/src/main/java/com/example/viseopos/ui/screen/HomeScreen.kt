package com.example.viseopos.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.viseopos.R
import com.example.viseopos.ui.components.ButtonFacial
import com.example.viseopos.ui.navigation.AppDestinations
import com.example.viseopos.ui.theme.ViseoPosTheme
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )) },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(AppDestinations.SETTINGS_ROUTE)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription =  "Accéder aux paramètres"
                        )
                    }
                },
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logoviseo),
                contentDescription = stringResource(id = R.string.home_access_odoo_prompt),
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )
            Text(
                text = stringResource(id = R.string.home_welcome_message),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.home_access_odoo_prompt),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
//            ButtonFacial(navController = navController)
  //          Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    navController.navigate(AppDestinations.AUTH_VIA_CODE_ROUTE)
                }
            ) {
                Text(text = "Connexion via Code Pin")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun HomeScreenPreview() {
    ViseoPosTheme {
        val dummyNavController = rememberNavController()
        HomeScreen(navController = dummyNavController)
    }
}