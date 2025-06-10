package com.example.viseopos.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.viseopos.R
import com.example.viseopos.ui.navigation.AppDestinations
import com.example.viseopos.ui.theme.OnViseoCyanBlue
import com.example.viseopos.ui.theme.ViseoCyanBlue

@Composable
fun ButtonFacial(navController: NavHostController){
    Button(
        onClick = {
            navController.navigate(AppDestinations.FACIAL_RECOGNITION_ROUTE)
        },
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ViseoCyanBlue,
            contentColor = OnViseoCyanBlue
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Face,
            contentDescription = stringResource(id = R.string.facial_recognition_button_icon_desc),
            modifier = Modifier.size(androidx.compose.material3.ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(androidx.compose.material3.ButtonDefaults.IconSpacing))
        Text(stringResource(id = R.string.home_facial_recognition_button))
    }
}