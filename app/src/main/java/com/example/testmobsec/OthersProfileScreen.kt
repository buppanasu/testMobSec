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
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
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
fun OthersProfileScreen(navController: NavController = rememberNavController()){
    Column(modifier = Modifier.fillMaxSize()){
        Row(modifier = Modifier.weight(2f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            //Text("Test1")
            Spacer(modifier = Modifier.width(35.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "",
                modifier = Modifier.size(100.dp).clip(CircleShape).border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(){
                Text("Sara Mathew", modifier = Modifier.paddingFromBaseline(top = 20.dp), fontSize = 23.sp)
                Text("Location: Bangalore, India",)

            }
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Top){
            Column(){
                Text("1532", fontWeight = FontWeight.Bold, fontSize = 25.sp)
                Text("Posts")
            }
            //Divider(thickness = 2.dp, color = Color.Gray)
            Column(){
                Text("4310", fontWeight = FontWeight.Bold, fontSize = 25.sp)
                Text("Followers")
            }
            //Divider(thickness = 2.dp, color = Color.Gray)
            Column(){
                Text("1310", fontWeight = FontWeight.Bold, fontSize = 25.sp)
                Text("Following")
            }
        }
        Row(modifier = Modifier.weight(1.5f).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Top){
            //Text("Test3")
            Button( onClick = {}){
                Text("Message")
            }
            Button( onClick = {}){
                Text("+Follow")
            }
        }
        Divider(thickness = 2.dp, color = Color.LightGray)
        Column(modifier = Modifier.weight(5f).background(Color.Magenta)){
            Text("Test4")
        }



    }
}



