package com.example.testmobsec

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testmobsec.util.UserRole
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController = rememberNavController()){

    var email:String by remember { mutableStateOf("") }
    var password:String by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center){
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
            OutlinedTextField(
                value = email,
                onValueChange = {email = it} ,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Email") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = {password = it} ,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Password") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button( onClick = {

                //Attempt to login using firebase authentication
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful){
                            //Login Successful, handle success
                            val user = task.result.user!!
                            Log.d("LoginScreen", "Logged in user: ${user.email}")
                            Toast.makeText(context, "Correct email and password", Toast.LENGTH_SHORT).show()
                            navController.navigate("home_screen")
                        }
                        else{
                            //Login failed, handle error
                            Log.e("LoginScreen", "Login Error: ${task.exception}")
                            //Show an error message to the user
                            Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                    }



            }){
                Text("Login Here")
            }
            Button(
                onClick = {
                    navController.navigate("register_screen")
                    // Navigate to the signup screen (replace with your navigation logic)
                    //Navigator.push(context, MaterialPageRoute(builder: { context } => RegisterScreen(sharedViewModel)))
                }
            ) {
                Text("New User? Sign Up")
            }
        }
    }
}