package com.example.testmobsec

import android.icu.text.SimpleDateFormat
import android.util.Log
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
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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


@Composable
fun ProfileScreen(
    navController: NavController = rememberNavController()
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val postsViewModel: PostViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }

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
           ProfileTopSection(navController,profileViewModel)
            TabRowSection(selectedTab = selectedTab, onTabSelected = { tab ->
                selectedTab = tab
            }, postsViewModel = postsViewModel, profileViewModel)


        }

    }


}
@Composable
fun TabRowSection(selectedTab: Int, onTabSelected: (Int) -> Unit,
                  postsViewModel: PostViewModel,profileViewModel: ProfileViewModel){
    // Observe the posts list from the ViewModel
    val posts by postsViewModel.posts.collectAsState(initial = emptyList())
    TabRow(selectedTabIndex = selectedTab) {
        // Replace with your tabs
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0)
                },
            text = { Text("Posts") }



        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = { Text("Tweets & Replies") }
        )
        Tab(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            text = { Text("Media") }
        )
        Tab(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            text = { Text("Likes") }
        )
    }
    // Content below tabs
    // This is a placeholder, replace it with your content based on the selected tab
//    Box(modifier = Modifier
////        .fillMaxSize()
//        )
    if (selectedTab == 0) {
        postsViewModel.fetchPostsForUser()
    }
    when (selectedTab) {
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(35.dp))
                        profileImageUrl?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }  ?: Text("No profile image available")
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            Text(text = name, fontWeight = FontWeight.Bold,
                                style = TextStyle(fontSize = 18.sp)
                            )
                            Divider()
                            Text(text = content, style = TextStyle(fontSize = 14.sp))
                            Text(text = formatDate(timestamp), style = TextStyle(fontSize = 12.sp))
                            Spacer(modifier = Modifier.height(4.dp))
                            IconButton(onClick = { /* Handle comment action */ }) {
                                Icon(Icons.Filled.Comment, contentDescription = "Comment")
                            }
                        }

                        // Like button with real-time color change based on like status
                        val likeIconColor = if (isLiked) Color.Blue else Color.Gray
                        IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                            Icon(Icons.Filled.ThumbUp, contentDescription = "Like", tint = likeIconColor)
                        }

                        // Display likes count, updating in real-time
                        if (likesCount > 0) {
                            Text(text = "$likesCount")
                        }
                    }
                    Divider()
                }
            }
        }
        1 -> {
            Text("Test")
            // Content for the second tab
        }

        3->{
            postsViewModel.fetchLikedPosts()
            val likedPosts by postsViewModel.likedPosts.collectAsState()
//            val profileImageUrl by profileViewModel.profileImageUrl.collectAsState()
            val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()

            // Display posts in the first tab
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
                    LaunchedEffect(userId) {
                        profileViewModel.fetchProfileImageUrlByUserId(userId)
                    }
                    val imageUrl = profileImageUrls[userId]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(35.dp))
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        ) ?: Text("No profile image available")
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(
                            modifier = Modifier
                                .weight(1.5f)
                                .padding(8.dp)
                        ) {
                            Text(text = name, fontWeight = FontWeight.Bold,
                                style = TextStyle(fontSize = 18.sp)
                            )
                            Divider()
                            Text(text = content, style = TextStyle(fontSize = 14.sp))
                            Text(text = formatDate(timestamp), style = TextStyle(fontSize = 12.sp))
                            Spacer(modifier = Modifier.height(4.dp))
                            IconButton(onClick = { /* Handle comment action */ }) {
                                Icon(Icons.Filled.Comment, contentDescription = "Comment")
                            }
                        }

                        // Like button with real-time color change based on like status
                        val likeIconColor = if (isLiked) Color.Blue else Color.Gray
                        IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                            Icon(Icons.Filled.ThumbUp, contentDescription = "Like", tint = likeIconColor)
                        }

                        // Display likes count, updating in real-time
                        if (likesCount > 0) {
                            Text(text = "$likesCount")
                        }
                    }
                    Divider()
                }
            }


        }
    }
}
@Composable
fun ProfileTopSection(navController: NavController, profileViewModel: ProfileViewModel) {
    val name by profileViewModel.name.collectAsState()
    val profileImageUrl by profileViewModel.profileImageUrl.collectAsState()

    // Hardcoded follower and following counts
    val followers = "400"
    val following = "100"
    val posts = "42"

    LaunchedEffect(key1 = true) {
        profileViewModel.fetchProfileImageUrl() // This is your existing method to fetch the image URL
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(8.dp))

        name?.let {
            Text(
                text = it,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(text = "Hi I'm lorem ipsum!!", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = posts, fontWeight = FontWeight.Bold)
                Text(text = "Posts")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = followers, fontWeight = FontWeight.Bold)
                Text(text = "Followers")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = following, fontWeight = FontWeight.Bold)
                Text(text = "Following")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { navController.navigate("edit_profile_screen") }) {
                Text("Edit Profile")
            }
            //USE THE BELOW BUTTON FOR WHEN VIEWING OTHER PEOPLE's PROFILE, so need implement the logic
//            Button(onClick = { /* Implement Follow action */ }) {
//                Text("+Follow")
//            }
        }
    }
}

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