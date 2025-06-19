package com.example.viseopos.ui.screen

import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.viseopos.ui.navigation.AppDestinations
import com.example.viseopos.ui.theme.ViseoPosTheme
import com.example.viseopos.ui.viewModel.OdooAuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthViaCodeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
     odooAuthViewModel: OdooAuthViewModel = viewModel()
) {
    val pinLength = 5
    var pinValue by remember { mutableStateOf(List(pinLength) { "" }) }
    val focusRequesters = remember { List(pinLength) { FocusRequester() } }
    val isLoading by odooAuthViewModel.isLoading.collectAsState()
    val authError by odooAuthViewModel.errorMessage.collectAsState()
    val token by odooAuthViewModel.token.collectAsState()

    LaunchedEffect(token) {
        if (token != null && token!!.isNotBlank()) {
            navController.navigate(AppDestinations.WEB_ODOO_ROUTE + "/$token")
            odooAuthViewModel.consumeToken()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Authentification") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(AppDestinations.HOME_SCREEN_ROUTE)
                       }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour à l'accueil"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Entrez votre code PIN",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pinValue.forEachIndexed { index, value ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                val newPin = pinValue.toMutableList()
                                newPin[index] = newValue
                                pinValue = newPin

                                if (newValue.isNotEmpty() && index < pinLength - 1) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .focusRequester(focusRequesters[index]),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
            authError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val enteredPin = pinValue.joinToString("")
                    if (enteredPin.length == pinLength) {
                        odooAuthViewModel.fetchTokenToMatricule(enteredPin)

                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && pinValue.joinToString("").length == pinLength
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Acceder à Odoo")
                }
            }
        }
    }
}