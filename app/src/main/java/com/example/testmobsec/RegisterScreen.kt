package com.example.testmobsec

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.testmobsec.util.*

import com.example.testmobsec.util.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
//fun RegisterScreen(haredViewModel: SharedViewModel)
fun RegisterScreen(sharedViewModel: SharedViewModel){
    var email:String by remember { mutableStateOf("") }
    var name:String by remember { mutableStateOf("") }
    var password:String by remember { mutableStateOf("") }
    // State to track the selected role
    var selectedRole by remember { mutableStateOf(UserRole.USER) }



    val context = LocalContext.current


    Column(modifier = Modifier.fillMaxSize()){

        //This column is for filling up of input
        Column(modifier = Modifier.weight(6f).padding(20.dp), verticalArrangement = Arrangement.SpaceEvenly){
            Text("Sign up!", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            OutlinedTextField(
                value = name,
                onValueChange = {name = it} ,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Username") }
            )
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
            Text(text = "Role", fontWeight = FontWeight.Bold, fontSize = 25.sp)

            // Radio button for user
            Row {
                RadioButton(
                    selected = selectedRole == UserRole.USER,
                    onClick = { selectedRole = UserRole.USER }
                )
                Text("User", modifier = Modifier.paddingFromBaseline(top = 28.dp))
            }

            // Radio button for event manager
            Row {
                RadioButton(
                    selected = selectedRole == UserRole.EVENT_MANAGER,
                    onClick = { selectedRole = UserRole.EVENT_MANAGER }
                )
                Text("Event Manager", modifier = Modifier.paddingFromBaseline(top = 28.dp))
            }


        }

        Column(modifier = Modifier.weight(4f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){

            Button( onClick = {
                val userData = UserData(
                    name = name,
                    email = email,
                    password = password,
                    role = selectedRole
                )

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){

                            //User created successfully, store additional data in Firestore
                            val userId = task.result.user!!.uid
                            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                            userRef.set(userData)
                                .addOnSuccessListener{
                                    //Store data successfully, handle success
                                    sharedViewModel.saveData(userData = userData, context = context)
                                }
                                .addOnFailureListener{e ->
                                    //Handle firestore error
                                    Log.e("RegisterScreen","Error storing user data: $e")
                                }
                        }
                        else{
                            //Handle authentication error
                            Log.e("RegisterScreen", "error creating user: ${task.exception}")
                        }
                    }

                //sharedViewModel.saveData(userData = userData, context = context)

            }){
                Text("Register")
            }
            Button( onClick = {}){
                Text("Already Registered? Login Here")
            }

        }

    }
}