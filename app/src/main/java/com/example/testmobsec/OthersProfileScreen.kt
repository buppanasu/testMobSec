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
import androidx.compose.foundation.layout.paddingFromBaseline
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
fun OthersProfileScreen(navController: NavController = rememberNavController(), userId: String){
    val profileViewModel: ProfileViewModel = viewModel()
    val postsViewModel: PostViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }

    // The Scaffold composable provides a consistent layout structure with a top app bar and padding
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        // BottomBar or FloatingActionButton can be added here if needed
    ){paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)){
            OthersProfileTopSection(navController,profileViewModel, postsViewModel, userId)
            OthersTabSection(selectedTab = selectedTab, onTabSelected = { tab ->
                selectedTab = tab
            }, postsViewModel = postsViewModel, profileViewModel, navController, userId)

        }

    }

}
@Composable
fun OthersProfileTopSection(navController: NavController,
                      profileViewModel: ProfileViewModel, postsViewModel: PostViewModel, userId: String){
    val profileImageUrl by profileViewModel.profileImageUrl.collectAsState()
    val postsCount by postsViewModel.postsCount.collectAsState()
    val userName by profileViewModel.userName.collectAsState()
    val followersCount by profileViewModel.followersCount.collectAsState()
    val followingCount by profileViewModel.followingCount.collectAsState()
    LaunchedEffect(true) {
        profileViewModel.fetchProfileImageUrl(userId)
        postsViewModel.fetchPostsCountForCurrentUser(userId)
        profileViewModel.fetchUserNameByUserId(userId)
        profileViewModel.fetchFollowCounts(userId)
    }
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Text("Test1")
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
        Column() {
            userName?.let {
                Text(text = it,
                    modifier = Modifier.paddingFromBaseline(top = 20.dp),
                    fontSize = 23.sp)
            }


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
        //Divider(thickness = 2.dp, color = Color.Gray)
        Column() {
            Text("$followersCount", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("Followers")
        }
        //Divider(thickness = 2.dp, color = Color.Gray)
        Column() {
            Text("$followingCount", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("Following")
        }
    }
    Row(
        modifier = Modifier
            .padding(vertical = 40.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {

        val isFollowing by profileViewModel.isFollowing.collectAsState(initial = false)
        // Check the following state on composition
        LaunchedEffect(key1 = userId) {
            profileViewModel.checkIfFollowing(userId)
        }

        Button(
            onClick = {
                profileViewModel.toggleFollowUser(userId)
            }
        ) {
            Text(text = if (isFollowing == true) "Unfollow" else "Follow")
        }
    }
}

@Composable
fun OthersTabSection(selectedTab: Int, onTabSelected: (Int) -> Unit,
                  postsViewModel: PostViewModel,profileViewModel: ProfileViewModel
                  ,navController: NavController, userId: String){
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
            text = { Text("Comments") }
        )

        Tab(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            text = { Text("Likes") }
        )
    }


    when (selectedTab) {
        0 -> {
            postsViewModel.fetchPostsForUser(userId)
            val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()
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
                        imageUrl?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(40.dp)
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
            postsViewModel.fetchCommentedPosts(userId)
            val commentedPosts by postsViewModel.commentedPosts.collectAsState()
            val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()
            // Display posts in the first tab
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
                    val commentedUserId = userDocRef?.id.toString()
                    val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
                    val commentsCount by commentsCountFlow.collectAsState()
                    LaunchedEffect(commentedUserId) {
                        profileViewModel.fetchProfileImageUrlByUserId(commentedUserId)
                    }
                    val imageUrl = profileImageUrls[commentedUserId]
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

        2->{
            postsViewModel.fetchLikedPosts(userId)
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
                    val likedUserId = userDocRef?.id.toString()
                    val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
                    val commentsCount by commentsCountFlow.collectAsState()
                    LaunchedEffect(likedUserId) {
                        profileViewModel.fetchProfileImageUrlByUserId(likedUserId)
                    }
                    val imageUrl = profileImageUrls[likedUserId]
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



