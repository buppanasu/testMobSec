package com.example.testmobsec

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.testmobsec.viewModel.PostViewModel


@Composable
fun HomeScreen(navController: NavController = rememberNavController()) {
    val postsViewModel: PostViewModel = viewModel()
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ){
            paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            HomePostsSection(postsViewModel)


        }


        }
    }

@Composable
fun HomePostsSection(postsViewModel: PostViewModel){
    val posts by postsViewModel.posts.collectAsState(initial = emptyList())
    postsViewModel.fetchPostsForHome()
    // Display posts in the first tab
    LazyColumn {
        items(posts) { postMap ->
            val name = postMap["userName"] as? String?: "No Content"
            val content = postMap["content"] as? String ?: "No Content"
            val timestamp = postMap["timestamp"]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                IconButton(onClick = { /* Handle like action */ }) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = "Like")
                }
            }
            Divider()
        }
    }
}





