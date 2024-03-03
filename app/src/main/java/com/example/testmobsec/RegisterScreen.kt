package com.example.testmobsec

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

import com.example.testmobsec.util.*

import com.example.testmobsec.util.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
//fun RegisterScreen(haredViewModel: SharedViewModel)
fun RegisterScreen(sharedViewModel: SharedViewModel,navController: NavController = rememberNavController()){
    var email:String by remember { mutableStateOf("") }
    var name:String by remember { mutableStateOf("") }
    var password:String by remember { mutableStateOf("") }
    // State to track the selected role
    var selectedRole by remember { mutableStateOf(UserRole.USER) }

    // Input Validation Variables
    var emailError: String by remember { mutableStateOf("") }
    var nameError: String by remember { mutableStateOf("") }
    var passwordError: String by remember { mutableStateOf("") }


    val context = LocalContext.current

    // Validation Functions
    fun validateEmail(): Boolean {
        if (email.isEmpty()) {
            emailError = "Email cannot be empty"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid email format"
            return false
        }
        emailError = ""
        return true
    }

    fun validateName(): Boolean {
        if (name.isEmpty()) {
            nameError = "Name cannot be empty"
            return false
        }
        nameError = ""
        return true
    }

    fun validatePassword(): Boolean {
        if (password.isEmpty()) {
            passwordError = "Password cannot be empty"
            return false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            return false
        }
        passwordError = ""
        return true
    }

        //This column is for filling up of input
        Column(modifier = Modifier.fillMaxSize().padding(50.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
            Image(
                painter = painterResource(id = R.drawable.signupscreenimage),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Username") },
                isError = nameError.isNotEmpty(), // Highlight the text field if there's an error
                label = {
                    // This logic ensures the label "Username" remains unless there's an error
                    if (nameError.isNotEmpty()) {
                        Text(nameError)
                    } else {
                        Text("Username")
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Email") },
                isError = emailError.isNotEmpty(),
                label = {
                    if (emailError.isNotEmpty()) {
                        Text(emailError)
                    } else {
                        Text("Email")
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Password") },
                isError = passwordError.isNotEmpty(),
                label = {
                    if (passwordError.isNotEmpty()) {
                        Text(passwordError)
                    } else {
                        Text("Password")
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            // Radio button for user
            Row {
                Text(text = "Signup as:", fontSize = 15.sp, modifier = Modifier.paddingFromBaseline(top = 28.dp))
                RadioButton(
                    selected = selectedRole == UserRole.USER,
                    onClick = { selectedRole = UserRole.USER }
                )
                Text("User", modifier = Modifier.paddingFromBaseline(top = 28.dp))
                // Radio button for event manager
                RadioButton(
                    selected = selectedRole == UserRole.EVENT_MANAGER,
                    onClick = { selectedRole = UserRole.EVENT_MANAGER }
                )
                Text("Event Manager", modifier = Modifier.paddingFromBaseline(top = 28.dp))
            }

            Button(onClick = {
                var isValid = true // Assuming inputs are valid initially

                // Perform validation checks
                if (!validateEmail()) isValid = false
                if (!validateName()) isValid = false
                if (!validatePassword()) isValid = false

                // Proceed if all inputs are valid
                if (isValid) {
                    // Firebase authentication and data storage logic
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // User created successfully, now store additional data
                                val userId = task.result.user!!.uid
                                val userData = UserData(name = name, email = email, role = selectedRole)
                                FirebaseFirestore.getInstance().collection("users").document(userId).set(userData)
                                    .addOnSuccessListener {
                                        // Data stored successfully
                                        sharedViewModel.saveData(userData = userData, context = context)
                                        Toast.makeText(context, "Register Success!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("login_screen")
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle errors in storing user data
                                        Log.e("RegisterScreen", "Error storing user data: $e")
                                    }
                            } else {
                                // Handle authentication errors
                                Log.e("RegisterScreen", "Error creating user: ${task.exception}")
                            }
                        }
                } else {
                    // Optionally, show a toast or log if validation fails
                    Toast.makeText(context, "Please check your inputs", Toast.LENGTH_SHORT).show()
                }
            }){
                Text("Register")
            }
            Button( onClick = {navController.navigate("login_screen")}){
                Text("Login Here")
            }
        }

}