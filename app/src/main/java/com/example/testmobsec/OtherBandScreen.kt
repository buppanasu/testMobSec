package com.example.testmobsec

import BandViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.testmobsec.util.Band
@Composable
fun OtherBandScreen(navController: NavController, bandId: String, bandViewModel: BandViewModel) {
    // Collect the band details state from the BandViewModel
    val bandDetails by bandViewModel.bandDetails.collectAsState()
    //val memberNames = remember { mutableStateListOf<String>() }
    val memberNames by bandViewModel.memberNames.collectAsState()

    // Fetch band details when the composable enters the composition
    LaunchedEffect(bandId) {
        bandViewModel.fetchBandDetails(bandId)
    }

    // When band details change, fetch member names
    LaunchedEffect(bandDetails) {
        bandDetails?.members?.let { members ->
            bandViewModel.fetchBandMemberNames(members)
        }
    }

    // UI for displaying band details
    Scaffold(
        topBar = { TopAppBarContent(navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ) {paddingValues ->
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                Text(
                    text = bandDetails?.bandName ?: "Loading...",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                // Image placeholder for the band
                Image(
                    painter = rememberAsyncImagePainter(bandDetails?.imageUrl),
                    contentDescription = "Band Image",
                    modifier = Modifier.size(200.dp)
                )

                //            // Display member names if available
                //            Column {
                //                Text("Members:", style = MaterialTheme.typography.headlineMedium)
                //                for (name in memberNames) {
                //                    Text(name, style = MaterialTheme.typography.bodySmall)
                //                }
                //            }

                // Follow or Unfollow button based on whether the user is following this band
                Button(onClick = {
                    // Handle follow/unfollow logic here
                }) {
                    Text(text = "Follow") /*TODO: Write logic for following/unfollowing*/
                }

                // Additional UI elements as required for your app
                // ...
                val tabTitles = listOf("Posts", "Artists", "Feed")
                var selectedTabIndex by remember { mutableStateOf(0) }
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Content based on selected tab
                when (tabTitles[selectedTabIndex]) {
                    "Posts" -> {
                        // TODO: Display posts content
                        Text("Posts content goes here", modifier = Modifier.padding(16.dp))
                    }


                    "Artists" -> {
                        Column {
                            Text("Members:", style = MaterialTheme.typography.headlineMedium)
                            memberNames.forEach { name ->
                                Text(name, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    "Feed" -> {
                        // TODO: Replace with actual feed content
                        Text("Feed content goes here", modifier = Modifier.padding(16.dp))
                    }

                }
            }
        }
    }
}

