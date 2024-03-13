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

@Composable
fun CreateOrJoinBandScreen(navController: NavController = rememberNavController()){
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(50.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
        //Spacer(modifier = Modifier.height(50.dp))
        Image(
            painter = painterResource(id = R.drawable.loginscreenimage),
            contentDescription = ""
        )
        Button(onClick = { navController.navigate("createband_screen") }) {
            Text("Create Band")
        }
        Button(onClick = { navController.navigate("joinband_screen") }) {
            Text("Join a band")
        }

    }

}