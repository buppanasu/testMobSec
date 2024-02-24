package com.example.testmobsec

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    navController: NavController = rememberNavController()
) {
    var selectedTab by remember { mutableStateOf(0) }
    Scaffold(
        bottomBar = { BottomAppBarContent(navController) }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
           ProfileTopSection(navController)
            TabRowSection(selectedTab) { tab ->
                selectedTab = tab
            }


        }

    }


}
@Composable
fun TabRowSection(selectedTab: Int, onTabSelected: (Int) -> Unit){
    TabRow(selectedTabIndex = selectedTab) {
        // Replace with your tabs
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = { Text("Tweets") }
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
    Box(modifier = Modifier
        .fillMaxSize()
        .background(if (selectedTab == 0) Color.LightGray else Color.White)) {
        Text(
            text = "Content of Tab $selectedTab",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
@Composable
fun ProfileTopSection(navController: NavController){
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
            Text(
                "Sara Mathew",
                modifier = Modifier.paddingFromBaseline(top = 20.dp),
                fontSize = 23.sp
            )
            Text("Location: Bangalore, India",)

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



@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen()
}