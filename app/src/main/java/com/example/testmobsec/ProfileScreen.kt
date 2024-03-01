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
            }, postsViewModel = postsViewModel)


        }

    }


}
@Composable
fun TabRowSection(selectedTab: Int, onTabSelected: (Int) -> Unit,
                  postsViewModel: PostViewModel){
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
        1 -> {
            Text("Test")
            // Content for the second tab
        }
    }
}
@Composable
fun ProfileTopSection(navController: NavController,
                      profileViewModel: ProfileViewModel){
    val name by profileViewModel.name.collectAsState()
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Text("Test1")
        Spacer(modifier = Modifier.width(35.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column() {
            name?.let {
                Text(text = "Name: $it",
                    modifier = Modifier.paddingFromBaseline(top = 20.dp),
                    fontSize = 23.sp)
            }
            Text(profileViewModel.getCreationTimestamp().toString()) //created at

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
            Text("1532", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("Posts")
        }
        //Divider(thickness = 2.dp, color = Color.Gray)
        Column() {
            Text("4310", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("Followers")
        }
        //Divider(thickness = 2.dp, color = Color.Gray)
        Column() {
            Text("1310", fontWeight = FontWeight.Bold, fontSize = 25.sp)
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
        //Text("Test3")
        Button(onClick = {navController.navigate("edit_profile_screen")}) {
            Text("Edit Profile")
        }
        Button(onClick = {}) {
            Text("+Follow")
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