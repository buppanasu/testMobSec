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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


@Composable
fun HomeScreen(navController: NavController = rememberNavController()) {
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ){
            paddingValues ->


    Column(modifier = Modifier.fillMaxSize()) {
        // Top-bar consisting of profile picture, app logo and settings
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.Black).padding(paddingValues),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,

            ) {

            //To replace with the profile picture
            Image(
                painter = painterResource(id = R.drawable.ngbobfong),
                contentDescription = "",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            //To replace with the logo
            Image(
                painter = painterResource(id = R.drawable.x),
                contentDescription = "",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )

            //To replace with the settings icon
            Image(
                painter = painterResource(id = R.drawable.settings),
                contentDescription = "",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
        //End of the row


        //Used as a spacer, abit stupid, need to relook
        Row(modifier = Modifier.height(20.dp).background(Color.Black).fillMaxWidth()) {}

        Divider(thickness = 0.4.dp, color = Color.LightGray)

        //LazyColumn should contain this
        Row(modifier = Modifier.background(Color.Black)) {

            //Profile picture
            Image(
                painter = painterResource(id = R.drawable.ngbobfong),
                contentDescription = "",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))

            //Name and post content
            Column() {
                //To replace with the name
                Text(
                    "Junjie ang",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                //To replace with the post content
                Text(
                    "I must say that Houstan was so much funnnnnnn. Thank you everybody for coming out tonight, for taking the time out of your busy day to come rock out at Minute maid park. For a good game. that was fucking funnnn",
                    color = Color.White
                )
            }
        }
    }

//  Example for LazyColumn - to refer to later
//        LazyColumn {
//            // Add a single item
//            item {
//                Text(text = "First item")
//            }
//
//            // Add 5 items
//            items(200) { index ->
//                Text(text = "Item: $index")
//            }
//
//            // Add another single item
//            item {
//                Text(text = "Last item")
//            }
//        }


    }
}



