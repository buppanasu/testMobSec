package com.example.testmobsec

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
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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


@Composable
fun HomeScreen(navController: NavController = rememberNavController()) {
    val postsViewModel: PostViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ){
            paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            HomePostsSection(postsViewModel, profileViewModel)


        }


        }
    }


@Composable
fun HomePostsSection(postsViewModel: PostViewModel, profileViewModel:ProfileViewModel){
    val posts by postsViewModel.posts.collectAsState(initial = emptyList())
    postsViewModel.fetchPostsForHome()
    val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()

    val likesCountMap = remember { mutableStateMapOf<String, Int>() }
    val userLikesMap = remember { mutableStateMapOf<String, Boolean>() }

    // Display posts in the first tab
    LazyColumn {
        items(posts) { postMap ->
            val userDocRef = postMap["userId"] as? DocumentReference
            val postId = postMap["postId"] as String
            val likesCountFlow = postsViewModel.getLikesCountFlow(postId)
            val likesCount by likesCountFlow.collectAsState()

            val userId = userDocRef?.id.toString()
            LaunchedEffect(userId) {
                profileViewModel.fetchProfileImageUrlByUserId(userId)
            }
            val name = postMap["userName"] as? String?: "No Content"
            val content = postMap["content"] as? String ?: "No Content"
            val timestamp = postMap["timestamp"]
            val imageUrl = profileImageUrls[userId]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Log.d("PROFILE SECTION", imageUrl)
                if (imageUrl != null) {

                    // Display the image from the URL
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // Placeholder or default image
                    Icon(Icons.Default.AccountCircle, contentDescription = "Default Profile", modifier = Modifier.size(40.dp))
                }

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

                }
                IconButton(onClick = { /* Handle comment action */ }) {
                    Icon(Icons.Filled.Comment, contentDescription = "Comment")
                }
                val isLiked by postsViewModel.isPostLikedByUser(postId).collectAsState(initial = false)
                val likeIconColor = if (isLiked) Color.Blue else Color.Gray
                IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = "Like", tint = likeIconColor)
                }
                // Conditional likes count text
                if (likesCount > 0) {
                    Text(text = "$likesCount")
                }
            }
            Divider()
        }
    }
}





