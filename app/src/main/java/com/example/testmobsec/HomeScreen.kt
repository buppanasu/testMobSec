package com.example.testmobsec


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.testmobsec.viewModel.BandViewModel
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import com.google.firebase.firestore.DocumentReference

// Define a Composable function for the HomeScreen. This function sets up the layout and navigation for the home screen.
@Composable
fun HomeScreen(navController: NavController = rememberNavController()) {
    // Obtain ViewModel instances for posts, profile, and bands.
    val postsViewModel: PostViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val bandViewModel: BandViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) } // Remember the selected tab state.
    // Setup the Scaffold layout which includes a top bar and a bottom bar.
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ){
            paddingValues ->
        // Define the content layout within the Scaffold.
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            // Include the HomeTabSection which is responsible for displaying different content based on the selected tab.
            HomeTabSection(selectedTab = selectedTab, onTabSelected = { tab ->
                selectedTab = tab
            }, postsViewModel = postsViewModel, profileViewModel, navController, bandViewModel)




        }


        }
    }
// HomePostsSection defines the UI and behavior for displaying a list of posts in the "Home" section made by all users.
@Composable
fun HomePostsSection(postsViewModel: PostViewModel, profileViewModel: ProfileViewModel, navController: NavController) {
    val posts by postsViewModel.posts.collectAsState(initial = emptyList())
    postsViewModel.fetchPostsForHome()
    val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()
    // Use LazyColumn to efficiently display a potentially large list of items.
    LazyColumn {
        items(posts) { postMap ->
            // For each post, extract relevant information like user ID, post ID, likes, and comments.
            val userDocRef = postMap["userId"] as? DocumentReference
            val postId = postMap["postId"] as String
            val likesCount by postsViewModel.getLikesCountFlow(postId).collectAsState()
            val commentsCount by postsViewModel.getCommentsCountFlow(postId).collectAsState()

            val userId = userDocRef?.id.toString()
            LaunchedEffect(userId) {
                profileViewModel.fetchProfileImageUrlByUserId(userId)
            }
            val name = postMap["userName"] as? String ?: "No Content"
            val content = postMap["content"] as? String ?: "No Content"
            val timestamp = postMap["timestamp"]
            val imageUrl = profileImageUrls[userId]

            // Display each post item using the PostItem composable.
            if (timestamp != null) {
                PostItem(
                    imageUrl = imageUrl,
                    name = name,
                    content = content,
                    timestamp = timestamp,
                    commentsCount = commentsCount,
                    likesCount = likesCount,
                    userId = userId,
                    postId = postId,
                    navController = navController,
                    postsViewModel = postsViewModel
                )
            }
        }
    }
}

// FollowingPostsSection is similar to HomePostsSection but tailored for displaying posts from followed bands and other users .
@Composable
fun FollowingPostsSection(
    postsViewModel: PostViewModel,
    profileViewModel: ProfileViewModel,
    navController: NavController
) {
    val posts by postsViewModel.posts.collectAsState(initial = emptyList())
    postsViewModel.fetchPostsFromFollowing()
    val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()

    LazyColumn {
        items(posts) { postMap ->
            val postId = postMap["postId"] as String
            val userId = (postMap["userId"] as? DocumentReference)?.id.toString()
            val name = postMap["userName"] as? String ?: "No Content"
            val content = postMap["content"] as? String ?: "No Content"
            val timestamp = postMap["timestamp"]
            val imageUrl = profileImageUrls[userId]
            val likesCount by postsViewModel.getLikesCountFlow(postId).collectAsState()
            val commentsCount by postsViewModel.getCommentsCountFlow(postId).collectAsState()

            if (timestamp != null) {
                PostItem(
                    imageUrl = imageUrl,
                    name = name,
                    content = content,
                    timestamp = timestamp,
                    commentsCount = commentsCount,
                    likesCount = likesCount,
                    userId = userId,
                    postId = postId,
                    navController = navController,
                    postsViewModel = postsViewModel
                )
            }
            Divider( thickness =  10.dp)
        }
    }
}


// HomeTabSection manages the tab layout at the top of the Home screen, allowing users to switch between different views.
            @Composable
            fun HomeTabSection(
                selectedTab: Int,
                onTabSelected: (Int) -> Unit,
                postsViewModel: PostViewModel,
                profileViewModel: ProfileViewModel,
                navController: NavController,
                bandViewModel: BandViewModel
            ) {
                // Observe the posts list from the ViewModel
                val posts by postsViewModel.posts.collectAsState(initial = emptyList())

                // State for nested tabs in the "Following" tab

                TabRow(selectedTabIndex = selectedTab) {

                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            onTabSelected(0)
                        },
                        text = { Text("For You") }


                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { onTabSelected(1) },
                        text = { Text("Following") }
                    )

                }
                when (selectedTab) {
                    0 -> {
                        // display the home posts section if the selected tab is 0
                        HomePostsSection(postsViewModel, profileViewModel, navController)
                    }

                    1 -> {

                        // display the following bands section if the selected tab is 1
                        FollowingBandsSection(postsViewModel, navController, bandViewModel)
                        Divider()

                    }

                }
            }
// Composable function to display an individual band item that the user follows. It shows the band's image and name.
@Composable
fun FollowedBandItem(bandName: String, imageUrl: String, bandId: String, navController: NavController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Box(
            modifier = Modifier
                .size(100.dp) // Set the size for the image
                .clip(CircleShape) // Clip the Box to be circle shaped
                .border(2.dp, Color.Gray, CircleShape) // Add a border around the circle
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "$bandName Image",
                modifier = Modifier
                    .fillMaxSize() // Fill the Box
            )
            Text( // Band name below the image
                text = bandName,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color(0xAA000000)) // Semi-transparent background for legibility
                    .padding(vertical = 2.dp) // Add some padding to the text
            )
        }
    }
}

// Composable function to display the sections related to bands that the current user is following, including a list of bands and their posts.
@Composable
fun FollowingBandsSection(postsViewModel: PostViewModel, navController: NavController, bandViewModel: BandViewModel) {
    // Trigger the fetching of bands the current user follows and their posts.
    bandViewModel.fetchBandsCurrentUserFollows()
    val followedBandPosts by  postsViewModel.followedBandPosts.collectAsState(initial = emptyList())
    postsViewModel.fetchPostsForFollowedBands()
    val followedBands by bandViewModel.followedBands.collectAsState()

    Column {
        // Row to display the "My Bands" title and an add button to navigate to the search band screen.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My Bands",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { navController.navigate("searchband_screen") }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        // Display the list of followed bands horizontally.
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(followedBands) { band ->
                // Use the FollowedBandItem composable to display each band.
                FollowedBandItem(
                    bandName = band["bandName"] as? String ?: "Unknown",
                    imageUrl = band["imageUrl"] as? String ?: "No Content",
                    bandId = band["bandId"] as? String ?: "No Content",
                    navController = navController
                )
                val bandName = band["bandName"] as? String ?: "Unknown"
                val imageUrl = band["imageUrl"] as? String ?: "No Content"
                val bandId = band["bandId"] as? String ?: "No Content"


            }

        }

        Divider() //Visual separation
        // LazyColumn to display posts from the followed bands.
        LazyColumn {
            items(followedBandPosts) { postMap ->
                // For each post, extract details and display them.
                val bandName = postMap["bandName"] as? String ?: "No Content"
                val content = postMap["content"] as? String ?: "No Content"
                val imageUrl = postMap["imageUrl"] as? String ?: "No Content"
                val timestamp = postMap["timestamp"]
                val postId = postMap["postId"] as String
                val bandDocRef = postMap["bandId"] as? DocumentReference
                val bandId = bandDocRef?.id.toString()
                val isLiked by postsViewModel.isPostLikedByUser(postId)
                    .collectAsState(initial = false)
                val likesCountFlow = postsViewModel.getLikesCountFlow(postId)
                val likesCount by likesCountFlow.collectAsState()
                val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
                val commentsCount by commentsCountFlow.collectAsState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("postDetails_screen/$postId") }
                        .padding(8.dp)
                ) {
                    // Profile image or placeholder
                    if (imageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "$bandName Image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Default Profile",
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Adjust the space as needed

                    // Column for bandName, content, and timestamp
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = bandName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatDate(timestamp), // Ensure this function returns the date as a string
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

// Action buttons and counts for comments and likes, placed below the profile image
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                ) {
                    // This space aligns the action buttons under the profile image
                    //Spacer(modifier = Modifier.height(6.dp)) // Height of the image plus additional padding

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val hasCommented by postsViewModel.hasUserCommented(postId).collectAsState(initial = false)
                        IconButton(onClick = { navController.navigate("comment_screen/$postId") }) {
                            Icon(
                                Icons.Filled.Comment,
                                contentDescription = "Comment",
                                tint = if (hasCommented) MaterialTheme.colorScheme.secondary else Color.Gray
                            )
                        }
                        if (commentsCount > 0) {
                            Text("$commentsCount")
                        }

                        Spacer(modifier = Modifier.width(24.dp)) // Space between comment and like buttons

                        val isLiked by postsViewModel.isPostLikedByUser(postId).collectAsState(initial = false)
                        IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                            Icon(
                                Icons.Filled.ThumbUp,
                                contentDescription = "Like",
                                tint = if (isLiked) MaterialTheme.colorScheme.secondary else Color.Gray
                            )
                        }
                        if (likesCount > 0) {
                            Text("$likesCount")
                        }
                    }
                }



                Divider()
            }
        }










    }
}
// Composable function to display a single post item with details such as the author's name, post content, and interaction buttons.
@Composable
fun PostItem(
    imageUrl: String?,
    name: String,
    content: String,
    timestamp: Any,
    commentsCount: Int,
    likesCount: Int,
    userId: String,
    postId: String,
    navController: NavController,
    postsViewModel: PostViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("postDetails_screen/$postId") }
            .padding(8.dp)
    ) {
        // Layout for the author's image or icon and the post details.
        Row(verticalAlignment = Alignment.Top) {
            if (imageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Default Profile",
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatDate(timestamp), // Make sure to implement this function
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
        // Interaction row for comments and likes.
        Row(verticalAlignment = Alignment.CenterVertically) {
            val hasCommented by postsViewModel.hasUserCommented(postId).collectAsState(initial = false)
            IconButton(onClick = { navController.navigate("comment_screen/$postId") }) {
                Icon(
                    imageVector = Icons.Filled.Comment,
                    contentDescription = "Comment",
                    tint = if (hasCommented) MaterialTheme.colorScheme.secondary else Color.Gray
                )
            }
            if (commentsCount > 0) {
                Text("$commentsCount")
            }
            Spacer(modifier = Modifier.width(24.dp))

            val isLiked by postsViewModel.isPostLikedByUser(postId).collectAsState(initial = false)
            IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                Icon(
                    imageVector = Icons.Filled.ThumbUp,
                    contentDescription = "Like",
                    tint = if (isLiked) MaterialTheme.colorScheme.secondary else Color.Gray
                )
            }
            if (likesCount > 0) {
                Text("$likesCount")
            }
        }
    }
    Divider()
}