package com.example.testmobsec

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


@Composable
fun UploadScreen(
    navController: NavController = rememberNavController()
) {
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ) {
        paddingValues->
        // Content of your screen
        Text("Upload", modifier = Modifier.padding(paddingValues))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUploadScreen() {
    UploadScreen()
}