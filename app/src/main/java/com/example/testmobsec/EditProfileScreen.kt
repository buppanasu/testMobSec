package com.example.testmobsec

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testmobsec.util.UserRole

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditProfileScreen(
    navController: NavController = rememberNavController()
) {
    Scaffold(
        bottomBar = { BottomAppBarContent(navController) }
    ) {
        // Content of your screen
        Column(modifier = Modifier.fillMaxSize()) {

            //This column is for filling up of input
            Row(){
                IconButton(onClick = { navController.navigate("profile_screen") }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Favorite")
                }
                Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, // Centers horizontally
                modifier = Modifier.fillMaxWidth()){// Ensure the Column takes up the full width){
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
                TextButton(onClick = {  }) {
                    Text("Change Profile Photo")
                }
            }


                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Name") }
                )

                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Username") }
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Email") }
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = {  },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Password") }
                )




            }
        }
    }


@Preview(showBackground = true)
@Composable
fun PreviewEditProfileScreen() {
    EditProfileScreen()
}