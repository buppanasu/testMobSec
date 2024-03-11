package com.example.testmobsec


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import com.google.firebase.firestore.DocumentReference


@Composable
fun HomeScreen(navController: NavController = rememberNavController()) {
    val postsViewModel: PostViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ){
            paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            HomeTabSection(selectedTab = selectedTab, onTabSelected = { tab ->
                selectedTab = tab
            }, postsViewModel = postsViewModel, profileViewModel, navController)

            


        }


        }
    }


@Composable
fun HomePostsSection(postsViewModel: PostViewModel, profileViewModel:ProfileViewModel, navController: NavController){
    val posts by postsViewModel.posts.collectAsState(initial = emptyList())
    postsViewModel.fetchPostsForHome()
    val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()



    // Display posts in the first tab
    LazyColumn {
        items(posts) { postMap ->
            val userDocRef = postMap["userId"] as? DocumentReference
            val postId = postMap["postId"] as String
            val likesCountFlow = postsViewModel.getLikesCountFlow(postId)
            val likesCount by likesCountFlow.collectAsState()
            val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
            val commentsCount by commentsCountFlow.collectAsState()

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
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("postDetails_screen/$postId")
                    },
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
                            .clickable {
                                // Navigate to OthersProfileScreen with userId
                                navController.navigate("othersProfile_screen/$userId")
                            }
                    )
                } else {
                    // Placeholder or default image
                    Icon(Icons.Default.AccountCircle, contentDescription = "Default Profile", modifier = Modifier.size(40.dp)
                        .clickable {
                            // Navigate to OthersProfileScreen with userId
                            navController.navigate("othersProfile_screen/$userId")
                        })
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

                val hasCommented by postsViewModel.hasUserCommented(postId).collectAsState(
                    initial = false
                )
                val commentedIconColor = if(hasCommented) Color.Magenta else Color.Gray
                IconButton(onClick = { navController.navigate("comment_screen/$postId") }) {
                    Icon(Icons.Filled.Comment, contentDescription = "Comment", tint = commentedIconColor)
                }
                if (commentsCount > 0) {
                    Text(text = "$commentsCount")
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
@Composable
fun FollowingPostsSection(postsViewModel: PostViewModel, profileViewModel:ProfileViewModel, navController: NavController){
    val posts by postsViewModel.posts.collectAsState(initial = emptyList())
    postsViewModel.fetchPostsFromFollowing()
    val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()



    // Display posts in the first tab
    LazyColumn {
        items(posts) { postMap ->
            val userDocRef = postMap["userId"] as? DocumentReference
            val postId = postMap["postId"] as String
            val likesCountFlow = postsViewModel.getLikesCountFlow(postId)
            val likesCount by likesCountFlow.collectAsState()
            val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
            val commentsCount by commentsCountFlow.collectAsState()

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
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("postDetails_screen/$postId")
                    },
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
                            .clickable {
                                // Navigate to OthersProfileScreen with userId
                                navController.navigate("othersProfile_screen/$userId")
                            }
                    )
                } else {
                    // Placeholder or default image
                    Icon(Icons.Default.AccountCircle, contentDescription = "Default Profile", modifier = Modifier.size(40.dp)
                        .clickable {
                            // Navigate to OthersProfileScreen with userId
                            navController.navigate("othersProfile_screen/$userId")
                        })
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

                val hasCommented by postsViewModel.hasUserCommented(postId).collectAsState(
                    initial = false
                )
                val commentedIconColor = if(hasCommented) Color.Magenta else Color.Gray
                IconButton(onClick = { navController.navigate("comment_screen/$postId") }) {
                    Icon(Icons.Filled.Comment, contentDescription = "Comment", tint = commentedIconColor)
                }
                if (commentsCount > 0) {
                    Text(text = "$commentsCount")
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
@Composable
fun HomeTabSection(selectedTab: Int, onTabSelected: (Int) -> Unit,
                  postsViewModel: PostViewModel,profileViewModel: ProfileViewModel
                  ,navController: NavController) {
    // Observe the posts list from the ViewModel
    val posts by postsViewModel.posts.collectAsState(initial = emptyList())
    TabRow(selectedTabIndex = selectedTab) {
        // Replace with your tabs
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
    when(selectedTab){
        0->{
            HomePostsSection(postsViewModel, profileViewModel, navController)
        }
        1->{
            FollowingPostsSection(postsViewModel, profileViewModel, navController)
        }
    }

}





