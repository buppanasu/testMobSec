package com.example.testmobsec


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.testmobsec.util.Band
import com.example.testmobsec.viewModel.BandViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import com.example.testmobsec.viewModel.ChatViewModel


@Composable
fun ChatListScreen(navController: NavController, bandViewModel: BandViewModel = viewModel()) {
    // State for managing search query
    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val allBands by bandViewModel.allBands.collectAsState(initial = emptyList())
    val profileViewModel: ProfileViewModel = viewModel()

    val followedUsers by profileViewModel.followedUsers.collectAsState()
    val userRole by profileViewModel.currentUserRole.collectAsState()
    val chatStarters by profileViewModel.chatStarters.collectAsState()

    // Fetch all bands when the screen is first composed
    LaunchedEffect(Unit) {
        bandViewModel.fetchAllBands()
        profileViewModel.fetchCurrentUserRole()
        profileViewModel.fetchUserDetailsFromFollowing()
        profileViewModel.fetchBandChatStarters()

    }

    // Filtered list of bands based on search query
    val filteredBands = allBands.filter {
        it.bandName.contains(searchQuery, ignoreCase = true)
    }
    val filteredUsers = followedUsers.filter {
            user ->
        val userName = user["name"] as? String ?: ""
        userName.contains(searchQuery, ignoreCase = true)
    }


    val filteredChatStarters = chatStarters.filter {
            user ->
        val userName = user["name"] as? String ?: ""
        userName.contains(searchQuery, ignoreCase = true)
    }

    // The Scaffold composable provides a consistent layout structure with a top app bar and padding
    Scaffold(
        topBar = { TopAppBarContent(navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            if(userRole=="ARTIST"){
                DisplayChatStarters(filteredChatStarters, navController)
            }
            else{


                // Tabs for selection
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Explore All Bands") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Followed Users") }
                    )
                }

                // Display content based on selected tab
                when (selectedTabIndex) {
                    0 -> DisplayBands(filteredBands, navController)
                    1 -> DisplayFollowedUsers(filteredUsers, navController)
                }

            }

        }
    }
}

@Composable
fun DisplayChatStarters(chatStarters: List<Map<String, Any>>, navController: NavController) {


    LazyColumn {
        items(chatStarters) { user ->
            val userId = user["userId"] as String
            val userName = user["name"] as String? ?: "Unknown User"

            UserItem(userId, userName, navController)
        }
    }
}

@Composable
fun DisplayFollowedUsers(users: List<Map<String, Any>>, navController: NavController) {
    // Assuming each user map contains "userId" and "userName" among other details
    LazyColumn {
        items(users) { user ->
            val userId = user["userId"] as String
            val userName = user["name"] as String? ?: "Unknown User"

            UserItem(userId, userName, navController)
        }
    }
}

@Composable
fun UserItem(userId: String, userName: String, navController: NavController) {
    // Replace with your desired user item layout
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { navController.navigate("chat_screen/$userId") },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = userName, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun DisplayBands(bands: List<Band>, navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        items(bands) { band ->
            ChatBandItem(band, navController)
        }
    }
}
@Composable
fun ChatBandItem(band: Band, navController: NavController, bandViewModel: BandViewModel = viewModel()) {
    Card(
        modifier = Modifier
            .padding(8.dp) // Add padding around the card for proper spacing in the grid.
            .fillMaxWidth()
            .height(180.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
            verticalArrangement = Arrangement.Center, // Center content vertically
            modifier = Modifier.fillMaxSize() // Ensure the column takes the full size of the card
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(data = band.imageUrl)
                        .error(R.drawable.ic_launcher_foreground)
                        .build()
                ),
                contentDescription = "Band Image",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally) // Ensure the image is centered
            )
            Spacer(modifier = Modifier.height(8.dp)) // Add space between image and text
            Text(
                text = band.bandName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // Ensure the text is centered
                    .padding(horizontal = 16.dp) // Add horizontal padding for the text
            )
            Spacer(modifier = Modifier.height(8.dp)) // Add space between text and button
            Button(
                onClick = {
                    navController.navigate("chat_screen/${band.bandId}")
                },
                modifier = Modifier.align(Alignment.CenterHorizontally) // Ensure the button is centered
            ) {
                Text("Chat")
            }
        }
    }
}

