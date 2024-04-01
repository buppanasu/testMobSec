package com.example.testmobsec

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// Composable function to display the screen with options for the user to either create a new band or join an existing one.
@Composable
fun CreateOrJoinBandScreen(navController: NavController = rememberNavController()) {
    // Arranging components vertically in the center of the screen, with a padding of 50.dp on all sides.
    Column(
        modifier = Modifier
            .fillMaxSize() // Fills the maximum available size.
            .padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Displaying an image at the top of the screen.
        Image(
            painter = painterResource(id = R.drawable.loginscreenimage),
            contentDescription = ""
        )

        // Button to navigate to the screen for creating a new band.
        Button(onClick = { navController.navigate("createband_screen") }) {
            Text("Create Band")
        }

        // Button to navigate to the screen for joining an existing band.
        Button(onClick = { navController.navigate("joinband_screen") }) {
            Text("Join a band")
        }

    }

}