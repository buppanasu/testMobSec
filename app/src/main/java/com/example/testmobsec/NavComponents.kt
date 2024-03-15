package com.example.testmobsec

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.testmobsec.viewModel.BandViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BottomAppBarContent(
    navController: NavController,
    bandViewModel: BandViewModel = viewModel()
) {
    // Assume _userRole is a State variable in your ViewModel
    val userRole by bandViewModel.userRole.collectAsState()

    LaunchedEffect(Unit) {
        bandViewModel.fetchUserRole()
    }

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        IconTextButton(navController, Icons.Default.Home, "Home", "home_screen")
        Spacer(Modifier.weight(1f, true))
        IconTextButton(navController, Icons.Default.Search, "Search", "searchband_screen")

        // Conditionally show the Band icon only for ARTIST role
        if (userRole == "ARTIST") {
            Spacer(Modifier.weight(1f, true))
            IconTextButton2(
                navController,
                Icons.Default.Add,
                "Band",
                onClick = { decideNavigationBasedOnBand(navController) }
            )
        }

        Spacer(Modifier.weight(1f, true))
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
        currentRoute?.startsWith("searchband_screen") == true -> "Search Band"
        currentRoute?.startsWith("band_screen") == true -> "Own Band"
        currentRoute?.startsWith("other_band_screen") == true -> "Other Band"
        currentRoute?.startsWith("chat_list_screen") == true -> "Chats"
        currentRoute?.startsWith("chat_screen") == true -> "Chat"
        currentRoute?.startsWith("edit_profile_screen") == true -> "Edit Profile Details"
        currentRoute?.startsWith("comment_screen") == true -> "Comments"
        currentRoute?.startsWith("postDetails_screen") == true -> "Post Details"
        currentRoute?.startsWith("createorjoinband_screen") == true -> "TBC: Create or join Band"
        currentRoute?.startsWith("createband_screen") == true -> "TBC: Create Band"
        currentRoute?.startsWith("feedback_screen") == true -> "Give Feedback"
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
                }else if(currentRoute.startsWith("home_screen")){
                    IconButton(onClick = { navController.navigate("chat_list_screen") }) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Chat"
                        )
                    }
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
@Composable
fun IconTextButton2(
    navController: NavController,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick) // Use the provided onClick lambda here
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = text)
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}


// Custom function for conditional navigation based on the user's bandId
fun decideNavigationBasedOnBand(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { documentSnapshot ->
            val bandId = documentSnapshot.getString("bandId")
            if (bandId.isNullOrEmpty()) {
                // User doesn't have a band, navigate to CreateOrJoinBandScreen
                navController.navigate("createorjoinband_screen")
            } else {
                // User has a band, navigate to BandScreen
                navController.navigate("band_screen/$bandId")
            }
        }.addOnFailureListener { e ->
            Log.e("NavComponent", "Error fetching user bandId", e)
            // Handle the error or navigate to a default screen
        }
    }
}