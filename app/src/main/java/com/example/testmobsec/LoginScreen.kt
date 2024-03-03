package com.example.testmobsec

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.testmobsec.util.UserRole
import com.google.firebase.auth.FirebaseAuth


@Composable
fun LoginScreen(navController: NavController = rememberNavController()) {

    var email: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }

    // Input Validation Variables
    var emailError: String by remember { mutableStateOf("") }
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
        emailError = "" // No error
        return true
    }

    fun validatePassword(): Boolean {
        if (password.isEmpty()) {
            passwordError = "Password cannot be empty"
            return false
        }
        passwordError = "" // No error
        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bandify",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Image(
            painter = painterResource(id = R.drawable.loginscreenimage),
            contentDescription = ""
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Email") },
            isError = emailError.isNotEmpty(),
            label = {
                if (emailError.isNotEmpty()) Text(emailError) else Text("Email")
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
                if (passwordError.isNotEmpty()) Text(passwordError) else Text("Password")
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            var isValid = true

            if (!validateEmail()) isValid = false
            if (!validatePassword()) isValid = false

            if (isValid) {
                //Attempt to login using firebase authentication
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Login Successful, handle success
                            val user = task.result.user!!
                            Log.d("LoginScreen", "Logged in user: ${user.email}")
                            Toast.makeText(
                                context,
                                "Correct email and password",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Navigates the next screen
                            navController.navigate("home_screen")
                        } else {
                            //Login failed, handle error
                            Log.e("LoginScreen", "Login Error: ${task.exception}")
                            //Show an error message to the user
                            Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

            } else {
                // If validation fails, show a toast message
                Toast.makeText(context, "Please check your inputs", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Login")
        }
        Button(
            onClick = {
                navController.navigate("register_screen")
                // Navigate to the signup screen (replace with your navigation logic)
                //Navigator.push(context, MaterialPageRoute(builder: { context } => RegisterScreen(sharedViewModel)))
            }
        ) {
            Text("No account? Signup here!")

        }
    }
}