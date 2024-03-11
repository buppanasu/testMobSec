package com.example.testmobsec

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContent(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val title = when{
        currentRoute?.startsWith("home_screen") == true -> "Home"
        currentRoute?.startsWith("profile_screen") == true -> "Profile"
        currentRoute?.startsWith("post_screen") == true -> "Upload Post"
        currentRoute?.startsWith("search_screen") == true -> "Search"
        currentRoute?.startsWith("edit_profile_screen") == true -> "Edit Profile Details"
        currentRoute?.startsWith("comment_screen") == true -> "Comments"
        currentRoute?.startsWith("postDetails_screen") == true -> "Post Details"
        currentRoute?.startsWith("createorjoinband_screen") == true -> "TBC: Create or join Band"
        currentRoute?.startsWith("createband_screen") == true -> "TBC: Create Band"
        currentRoute?.startsWith("othersProfile_screen") == true -> ""



        // Add more cases for other screens
        else -> "App"
    }

    CenterAlignedTopAppBar(
        title = { Text(text = title, color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            if(currentRoute == "edit_profile_screen"){
                IconButton(onClick = { navController.navigate("profile_screen")  }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
            else if (currentRoute != null) {
                if(currentRoute.startsWith("othersProfile_screen")){
                    IconButton(onClick = { navController.popBackStack()  }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }

        },
        actions = {
            if(currentRoute == "profile_screen") {
                // Display settings icon when on the profile screen
                IconButton(onClick = { navController.navigate("edit_profile_screen") }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Edit Profile Page"
                    )
                }
            }else if (currentRoute != null) {
                if(currentRoute.startsWith("comment_screen") || currentRoute.startsWith("post_screen") || currentRoute.startsWith("postDetails_screen")){
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                } else if(currentRoute.startsWith("othersProfile_screen")){
                    // No action if it starts with "othersProfile_screen"
                }
                } else{
                    IconButton(onClick = { navController.navigate("profile_screen") }) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile Page"
                        )
                    }
                }


        },

        )
}

@Composable
fun getCurrentRoute(navController: NavController): String? {
    // NavController's current destination can be observed to get the current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
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