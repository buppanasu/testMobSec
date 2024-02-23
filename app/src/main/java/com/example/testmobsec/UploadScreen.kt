package com.example.testmobsec

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UploadScreen(
    navController: NavController = rememberNavController()
) {
    Scaffold(
        bottomBar = { BottomAppBarContent(navController) }
    ) {
        // Content of your screen
        Text("Upload", modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUploadScreen() {
    UploadScreen()
}