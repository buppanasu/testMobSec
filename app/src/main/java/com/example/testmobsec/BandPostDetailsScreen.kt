package com.example.testmobsec


import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import com.google.firebase.firestore.DocumentReference

/// TODO: Band Post Details Screen if got time 
@Composable
fun BandPostDetailsScreen(
    navController: NavController = rememberNavController(),
    postId: String
) {
    val postsViewModel: PostViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val comments by postsViewModel.comments.collectAsState()
    val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()
    val postDetails by postsViewModel.selectedPostDetails.collectAsState()
    LaunchedEffect(postId) {
        postsViewModel.fetchPostByPostId(postId)
        postsViewModel.fetchCommentsForPost(postId)
    }

    // The Scaffold composable provides a consistent layout structure with a top app bar and padding
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        // BottomBar or FloatingActionButton can be added here if needed
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)) {
            postDetails?.let { post ->
                val postUserName = post["userName"] as? String ?: "Unknown"
                val postContent = post["content"] as? String ?: "No Content"
                val postUserDocRef = post["userId"] as? DocumentReference
                val postTimestamp = post["timestamp"]
                val userId = postUserDocRef?.id.toString()
                val postImageUrl = profileImageUrls[userId]
                LaunchedEffect(userId) {
                    profileViewModel.fetchProfileImageUrlByUserId(userId)
                }

                BandPostItem(userId,userName = postUserName, content = postContent, imageUrl = postImageUrl, timestamp = postTimestamp, navController)
                Divider()
                // Display posts in the first tab
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                ) {
                    items(comments) { postMap ->
                        val name = postMap["userName"] as? String ?: "No Content"
                        val content = postMap["comment"] as? String ?: "No Content"
                        val postDocRef = postMap["postId"] as? DocumentReference
                        val commentPostId = postDocRef?.id.toString()
                        val timestamp = postMap["timestamp"]
                        val isLiked by postsViewModel.isPostLikedByUser(commentPostId)
                            .collectAsState(initial = false)
                        val likesCountFlow = postsViewModel.getLikesCountFlow(commentPostId)
                        val likesCount by likesCountFlow.collectAsState()
                        val userDocRef = postMap["userId"] as? DocumentReference
                        val userId = userDocRef?.id.toString()
                        val commentsCountFlow = postsViewModel.getCommentsCountFlow(commentPostId)
                        val commentsCount by commentsCountFlow.collectAsState()
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
                                    text = name, fontWeight = FontWeight.Bold,
                                    style = TextStyle(fontSize = 18.sp)
                                )
                                Divider()
                                Text(text = content, style = TextStyle(fontSize = 14.sp))
                                Text(text = formatDate(timestamp), style = TextStyle(fontSize = 12.sp))
                                Spacer(modifier = Modifier.height(4.dp))

                            }

                            val hasCommented by postsViewModel.hasUserCommented(commentPostId).collectAsState(
                                initial = false
                            )
                            val commentedIconColor = if (hasCommented) Color.Magenta else Color.Gray
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
                            IconButton(onClick = { postsViewModel.toggleLike(commentPostId) }) {
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


        }

    }
}

@Composable
fun BandPostItem(userId: String, userName: String, content: String, imageUrl: String?, timestamp: Any?, navController: NavController) {
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Post content
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
                    .clickable {
                        // Navigate to OthersProfileScreen with userId
                        navController.navigate("othersProfile_screen/$userId")
                    }
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
                Text(text = formatDate(timestamp), style = TextStyle(fontSize = 12.sp))
                Spacer(modifier = Modifier.height(4.dp))

            }
        }
    }
}


