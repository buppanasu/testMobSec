package com.example.testmobsec

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.testmobsec.viewModel.BandViewModel
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import com.google.firebase.firestore.DocumentReference

@Composable
fun OtherBandScreen(navController: NavController, bandId: String, bandViewModel: BandViewModel) {
    // Collect the band details state from the BandViewModel
    val bandDetails by bandViewModel.bandDetails.collectAsState()
    val postsViewModel: PostViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    //val memberNames = remember { mutableStateListOf<String>() }
    val memberNames by bandViewModel.memberNames.collectAsState()
    val isFollowing by bandViewModel.isFollowing.collectAsState(initial = false)
    val bandPosts by postsViewModel.bandPosts.collectAsState(initial = emptyList())
    val bandFeedback by postsViewModel.bandFeedbacks.collectAsState(initial = emptyList())
    val followersCount by bandViewModel.followersCount.collectAsState()
    val bandPostsCount by postsViewModel.bandPostsCount.collectAsState()

    // State variables for UI
    val bandProfileImageUrl by bandViewModel.bandProfileImageUrl.collectAsState()
    val bandImagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data = bandProfileImageUrl)
            .error(R.drawable.ic_launcher_foreground) // Your placeholder image
            .build()
    )


    // Fetch band details when the composable enters the composition
    LaunchedEffect(bandId) {
        bandViewModel.fetchBandDetails(bandId)
        bandViewModel.checkIfFollowingBand(bandId)
        bandViewModel.fetchBandProfileImageUrl(bandId)
    }
    LaunchedEffect(true) {
        bandViewModel.fetchBandFollowerCount(bandId)
        postsViewModel.fetchPostsCountForBand(bandId)
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
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Posts: $bandPostsCount")
                    Text("Followers: $followersCount")

                }

                Row(){
                    Button(
                        onClick = {
                            bandViewModel.toggleFollowBand(bandId)
                        }
                    ) {
                        Text(text = if (isFollowing == true) "Unfollow" else "Follow")
                    }

                    Button(
                        onClick = {
                            navController.navigate("feedback_screen/$bandId")
                        }
                    ) {
                        Text(text = "Feedback")
                    }

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
                        postsViewModel.fetchPostsForBand(bandId)

                        LazyColumn {
                            items(bandPosts) { postMap ->
                                val bandName = postMap["bandName"] as? String ?: "No Content"
                                val content = postMap["content"] as? String ?: "No Content"
                                val timestamp = postMap["timestamp"]
                                val postId = postMap["postId"] as String
                                val isLiked by postsViewModel.isPostLikedByUser(postId)
                                    .collectAsState(initial = false)
                                val likesCountFlow = postsViewModel.getLikesCountFlow(postId)
                                val likesCount by likesCountFlow.collectAsState()
                                val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
                                val commentsCount by commentsCountFlow.collectAsState()


                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(35.dp))
                                    bandProfileImageUrl?.let { url ->
                                        Image(
                                            painter = bandImagePainter,
                                            contentDescription = "Band Image",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                    } ?: Text("No profile image available")
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = bandName, fontWeight = FontWeight.Bold,
                                            style = TextStyle(fontSize = 18.sp)
                                        )
                                        Divider()
                                        Text(text = content, style = TextStyle(fontSize = 14.sp))
                                        Text(
                                            text = formatDate(timestamp),
                                            style = TextStyle(fontSize = 12.sp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                    }
                                    val hasCommented by postsViewModel.hasUserCommented(postId)
                                        .collectAsState(
                                            initial = false
                                        )
                                    val commentedIconColor =
                                        if (hasCommented) Color.Magenta else Color.Gray
                                    IconButton(onClick = { navController.navigate("comment_screen/$postId") }) {
                                        Icon(
                                            Icons.Filled.Comment,
                                            contentDescription = "Comment",
                                            tint = commentedIconColor
                                        )
                                    }
                                    if (commentsCount > 0) {
                                        Text(text = "$commentsCount")
                                    }

                                    // Like button with real-time color change based on like status
                                    val likeIconColor = if (isLiked) Color.Blue else Color.Gray
                                    IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                                        Icon(
                                            Icons.Filled.ThumbUp,
                                            contentDescription = "Like",
                                            tint = likeIconColor
                                        )
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


                    "Artists" -> {
                        Column {
                            Text("Members:", style = MaterialTheme.typography.headlineMedium)
                            memberNames.forEach { name ->
                                Text(name, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    "Feed" -> {
                        postsViewModel.fetchFeedbackForBand(bandId)
                        val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()

                        LazyColumn {
                            items(bandFeedback) { postMap ->
                                val userName = postMap["userName"] as? String ?: "No Content"
                                val content = postMap["feedback"] as? String ?: "No Content"
                                val timestamp = postMap["timestamp"]
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
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    ) ?: Text("No profile image available")
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = userName, fontWeight = FontWeight.Bold,
                                            style = TextStyle(fontSize = 18.sp)
                                        )
                                        Divider()
                                        Text(text = content, style = TextStyle(fontSize = 14.sp))
                                        Text(
                                            text = formatDate(timestamp),
                                            style = TextStyle(fontSize = 12.sp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                    }

                                }
                                Divider()
                            }
                        }

                    }

                }
            }
        }
    }
}

