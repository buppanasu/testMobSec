package com.example.testmobsec

import android.app.AlertDialog
import android.content.Intent
import android.provider.Settings
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
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.content.ComponentName
import android.text.TextUtils
import com.example.testmobsec.util.EmulCheck


@Composable
fun LoginScreen(navController: NavController = rememberNavController()) {

    // Check if the app is running on an emulator
    var isEmulator by remember { mutableStateOf(EmulCheck.isRunningOnEmulator()) }


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

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(intent)
    }

    // Function to Navigate Users to Accessibility Package
    fun showAccessibilityInstructions(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Enable Accessibility Service")
            .setMessage("Please turn on 'Bandify' in the Accessibility settings for best performance.")
            .setPositiveButton("Go to Settings") { dialog, which ->
                openAccessibilitySettings(context)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to check if Accessibility Package is enabled
    fun isAccessibilityServiceEnabled(
        context: Context,
        service: Class<out MyAccessibilityService>
    ): Boolean {
        val expectedComponentName = ComponentName(context, service)

        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)

            if (enabledService != null && enabledService == expectedComponentName)
                return true
        }

        return false
    }

    // Check if the Accessibility Service is enabled
    val isAccessibilityEnabled =
        isAccessibilityServiceEnabled(context, MyAccessibilityService::class.java)

    if (!isAccessibilityEnabled) {
        showAccessibilityInstructions(context)
    }

    if (isEmulator) {
        Toast.makeText(
            context,
            "Running on an emulator. Login disabled for security reasons.",
            Toast.LENGTH_LONG
        ).show()
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
            placeholder = { Text(text = "Email") }
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
        Button(
            onClick = {
                if (!isEmulator) {
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
                                    Toast.makeText(
                                        context,
                                        "Invalid email or password",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
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
                                    )
                                        .show()
                                    //navController.navigate("createband_screen")
                                    navController.navigate("home_screen")
                                } else {
                                    //Login failed, handle error
                                    Log.e("LoginScreen", "Login Error: ${task.exception}")
                                    //Show an error message to the user
                                    Toast.makeText(
                                        context,
                                        "Invalid email or password",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }

                    } else {
                        // If validation fails, show a toast message
                        Toast.makeText(context, "Please check your inputs", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            },
            enabled = !isEmulator,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Button(
            onClick = {
                if (!isEmulator) {
                    navController.navigate("register_screen")
                } else {
                    // Optionally, you could show a toast if you want to inform the user
                    Toast.makeText(
                        context,
                        "Signup disabled on emulator for security reasons.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // Navigate to the signup screen (replace with your navigation logic)
                //Navigator.push(context, MaterialPageRoute(builder: { context } => RegisterScreen(sharedViewModel)))
            },
            enabled = !isEmulator,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("No account? Signup here!")

        }
    }
}