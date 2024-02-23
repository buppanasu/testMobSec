package com.example.testmobsec

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomAppBarContent(
    navController: NavController
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        IconTextButton(navController, Icons.Default.Home, "Home", "home_screen")
        Spacer(Modifier.weight(1f))
        IconTextButton(navController, Icons.Default.Search, "Search", "search_screen")
        Spacer(Modifier.weight(1f))
        // Add icons for Chat and Profile if available in your Icons object
        IconTextButton(navController, Icons.Default.Add, "Upload", "upload_screen")
        Spacer(Modifier.weight(1f))
        IconTextButton(navController, Icons.Default.Person, "Profile", "profile_screen")
    }
}

@Composable
fun IconTextButton(
    navController: NavController,
    icon: ImageVector,
    text: String,
    navigateTo: String
) {
    Column(
        modifier = Modifier
            .clickable { navController.navigate(navigateTo) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = text)
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}