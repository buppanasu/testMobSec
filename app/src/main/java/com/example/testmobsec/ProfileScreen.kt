package com.example.testmobsec

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import java.util.Locale
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.DocumentReference


// Composable function that represents the entire profile screen. It includes a top app bar, a bottom app bar, and a floating action button for navigation.
@Composable
fun ProfileScreen(
    navController: NavController = rememberNavController() // Allows navigation between composables
) {
    val profileViewModel: ProfileViewModel = viewModel() // ViewModel to fetch profile-related data
    val postsViewModel: PostViewModel = viewModel() // ViewModel to fetch posts-related data
    var selectedTab by remember { mutableStateOf(0) } // State for handling tab selection

    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        bottomBar = { BottomAppBarContent(navController) },
        floatingActionButton = { // Use the floatingActionButton parameter to add the IconButton at the bottom right
            FloatingActionButton(onClick = { navController.navigate("post_screen")}) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.End, // Position the button at the end (right)
//        isFloatingActionButtonDocked = false, // Set to false so the FAB is not docked in the BottomAppBar
    ) {
            paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
           ProfileTopSection(navController,profileViewModel, postsViewModel)
            TabRowSection(selectedTab = selectedTab, onTabSelected = { tab ->
                selectedTab = tab
            }, postsViewModel = postsViewModel, profileViewModel, navController)


        }

    }


}
// Composable function for displaying the tab row and its respective content based on the selected tab.
@Composable
fun TabRowSection(selectedTab: Int, onTabSelected: (Int) -> Unit,
                  postsViewModel: PostViewModel,profileViewModel: ProfileViewModel
,navController: NavController){
    // The function listens to changes in posts collection from the ViewModel.
    val posts by postsViewModel.posts.collectAsState(initial = emptyList())
    // TabRow to switch between different content sections: Posts, Comments, Likes.
    TabRow(selectedTabIndex = selectedTab) {
        // Definition of individual tabs
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0)
                },
            text = { Text("Posts") }



        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = { Text("Comments") }
        )

        Tab(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            text = { Text("Likes") }
        )
    }

    if (selectedTab == 0) {
        postsViewModel.fetchPostsForUser() //fetches posts the user has made
    }
    when (selectedTab) {
        // shows posts that the user has made
        0 -> {
            val profileImageUrl by profileViewModel.profileImageUrl.collectAsState()
            LaunchedEffect(true) {
                profileViewModel.fetchProfileImageUrl()
            }
            // Display posts in the first tab
            LazyColumn {
                items(posts) { postMap ->
                    val name = postMap["userName"] as? String?: "No Content"
                    val content = postMap["content"] as? String ?: "No Content"
                    val timestamp = postMap["timestamp"]
                    val postId = postMap["postId"] as String
                    val isLiked by postsViewModel.isPostLikedByUser(postId).collectAsState(initial = false)
                    val likesCountFlow = postsViewModel.getLikesCountFlow(postId)
                    val likesCount by likesCountFlow.collectAsState()
                    val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
                    val commentsCount by commentsCountFlow.collectAsState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 16.dp) // Adjust the end padding to control space between image and text column
                        ) {
                            // Profile image
                            if (profileImageUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(profileImageUrl),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(60.dp) // Adjust size to match the text height
                                        .clip(CircleShape)
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            } else {
                                Text("No profile image available", style = MaterialTheme.typography.bodyMedium)
                            }

                        }

                        // Texts column
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(text = content, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = formatDate(timestamp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    // Action icons and counts
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val hasCommented by postsViewModel.hasUserCommented(postId).collectAsState(initial = false)
                        val isLiked by postsViewModel.isPostLikedByUser(postId).collectAsState(initial = false)

                        // Comment icon with count
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { navController.navigate("comment_screen/$postId") }) {
                                Icon(
                                    Icons.Filled.Comment,
                                    contentDescription = "Comment",
                                    tint = if (hasCommented) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                            if (commentsCount > 0) {
                                Text(
                                    text = "$commentsCount",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Like icon with count
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                                Icon(
                                    Icons.Filled.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                            if (likesCount > 0) {
                                Text(
                                    text = "$likesCount",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }

                    Divider()
                }
            }
        }
        1 -> {
            //shows the posts user has commented on
            postsViewModel.fetchCommentedPosts() //fetches the posts the current user has commented on before
            val commentedPosts by postsViewModel.commentedPosts.collectAsState()
            val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()
            // Display comments and their data
            LazyColumn {
                items(commentedPosts) { postMap ->
                    val name = postMap["userName"] as? String?: "No Content"
                    val content = postMap["content"] as? String ?: "No Content"
                    val timestamp = postMap["timestamp"]
                    val postId = postMap["postId"] as String
                    val isLiked by postsViewModel.isPostLikedByUser(postId).collectAsState(initial = false)
                    val likesCountFlow = postsViewModel.getLikesCountFlow(postId)
                    val likesCount by likesCountFlow.collectAsState()
                    val userDocRef = postMap["userId"] as? DocumentReference
                    val userId = userDocRef?.id.toString()
                    val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
                    val commentsCount by commentsCountFlow.collectAsState()
                    LaunchedEffect(userId) {
                        profileViewModel.fetchProfileImageUrlByUserId(userId)
                    }
                    val imageUrl = profileImageUrls[userId]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top // Align to top for better control with varied text lengths
                    ) {
                        // Profile image
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            ) ?: Text("No profile image available", style = MaterialTheme.typography.bodyMedium)


                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Column for text content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = formatDate(timestamp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
// Action icons directly below the profile image
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val hasCommented by postsViewModel.hasUserCommented(postId).collectAsState(initial = false)
                        IconButton(onClick = { navController.navigate("comment_screen/$postId") }) {
                            Icon(Icons.Filled.Comment, contentDescription = "Comment", tint = if(hasCommented) Color.Magenta else Color.Gray)
                        }
                        if (commentsCount > 0) {
                            Text(text = "$commentsCount")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        val likeIconColor = if (isLiked) Color.Blue else Color.Gray
                        IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                            Icon(Icons.Filled.ThumbUp, contentDescription = "Like", tint = likeIconColor)
                        }
                        if (likesCount > 0) {
                            Text(text = "$likesCount")
                        }
                    }
                    Divider()

                }
            }

        }

        2->{
            postsViewModel.fetchLikedPosts() //fetches all post the current user has liked
            val likedPosts by postsViewModel.likedPosts.collectAsState()
            val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()


            // Display posts that the user has liked
            LazyColumn {
                items(likedPosts) { postMap ->
                    val name = postMap["userName"] as? String?: "No Content"
                    val content = postMap["content"] as? String ?: "No Content"
                    val timestamp = postMap["timestamp"]
                    val postId = postMap["postId"] as String
                    val isLiked by postsViewModel.isPostLikedByUser(postId).collectAsState(initial = false)
                    val likesCountFlow = postsViewModel.getLikesCountFlow(postId)
                    val likesCount by likesCountFlow.collectAsState()
                    val userDocRef = postMap["userId"] as? DocumentReference
                    val userId = userDocRef?.id.toString()
                    val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
                    val commentsCount by commentsCountFlow.collectAsState()
                    val hasCommented by postsViewModel.hasUserCommented(postId).collectAsState(initial = false)
                    val likeIconColor = if (isLiked) Color.Blue else Color.Gray
                    LaunchedEffect(userId) {
                        profileViewModel.fetchProfileImageUrlByUserId(userId)
                    }
                    val imageUrl = profileImageUrls[userId]
                    // This is the main Row that holds the image, texts, and icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Column for profile image and action icons
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            // Profile image
                            if (imageUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(60.dp) // Size adjusted for visibility
                                        .clip(CircleShape)
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            } else {
                                Text("No profile image available", style = MaterialTheme.typography.bodyMedium)
                            }


                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Column for text content next to the profile image
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = formatDate(timestamp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        // Action icons below the profile image
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Comments icon with count
                            IconButton(onClick = { navController.navigate("comment_screen/$postId")  }) {
                                Icon(Icons.Filled.Comment, contentDescription = "Comment", tint = if(hasCommented) Color.Magenta else Color.Gray)
                            }
                            if (commentsCount > 0) {
                                Text(text = "$commentsCount")
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Likes icon with count
                            IconButton(onClick = {postsViewModel.toggleLike(postId) }) {
                                Icon(Icons.Filled.ThumbUp, contentDescription = "Like", tint = likeIconColor)
                            }
                            if (likesCount > 0) {
                                Text(text = "$likesCount")
                            }
                        }
                    }



                    Divider() // Divider to separate posts
                }
            }


        }
    }
}

// This composable function displays the top section of the Profile screen. It shows the user's profile image,
// name, and counts of posts, followers, and following. It also includes a button to edit the profile.
@Composable
fun ProfileTopSection(navController: NavController, // Navigation controller to handle navigation events.
                      profileViewModel: ProfileViewModel, // ViewModel to fetch and hold profile data.
                      postsViewModel: PostViewModel){ // ViewModel to fetch and hold posts data.
    // Collect state from ProfileViewModel and PostViewModel.
    val name by profileViewModel.name.collectAsState()
    val profileImageUrl by profileViewModel.profileImageUrl.collectAsState()
    val postsCount by postsViewModel.postsCount.collectAsState()
    val followersCount by profileViewModel.followersCount.collectAsState()
    val followingCount by profileViewModel.followingCount.collectAsState()

    // Fetch profile image URL, posts count for the current user, and followers/following count upon composable instantiation.
    LaunchedEffect(true) {
        profileViewModel.fetchProfileImageUrl()
        postsViewModel.fetchPostsCountForCurrentUser()
        profileViewModel.fetchFollowCounts()
    }

    // Main column for the profile top section.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Display profile image if URL is available, else show a default icon.
        profileImageUrl?.let { url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            )
        } ?: Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Default Profile Image",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(4.dp))

        name?.let {
            Text(
                text = it,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        }

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        Column() {
            Text("$postsCount", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("Posts")
        }

        Column() {
            Text("$followersCount", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("Followers")
        }

        Column() {
            Text("$followingCount", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("Following")
        }
    }
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {

        Button(onClick = {navController.navigate("edit_profile_screen")}) {
            Text("Edit Profile")
        }

    }
}

// helper function to format the timestamp to simple date format of Month, date, year, hours and minutes
@Composable
fun formatDate(timestamp: Any?): String {
    return if (timestamp is com.google.firebase.Timestamp) {
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val date = timestamp.toDate()
        formatter.format(date)
    } else {
        "Unknown date"
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen()
}