package com.example.testmobsec

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testmobsec.util.UserRole
import com.example.testmobsec.viewModel.ProfileViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditProfileScreen(
    navController: NavController = rememberNavController(),

) {
    val profileViewModel: ProfileViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomAppBarContent(navController) }
    ) {
        paddingValue ->
        EditProfileSection(navController, paddingValue,profileViewModel)
    }
    }

@Composable
fun EditProfileSection(
    navController: NavController, paddingValue: PaddingValues,
    profileViewModel: ProfileViewModel
){
    val context = LocalContext.current // Get the context
    val name by profileViewModel.name.collectAsState()
    val email by profileViewModel.email.collectAsState()

    var nameText by remember { mutableStateOf(name ?: "") }
    var emailText by remember { mutableStateOf(email ?: "") }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    val isFormValid = remember(oldPassword, newPassword, confirmNewPassword) {
        oldPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty()
    }

    // state for reauth
    var reauthPassword by remember { mutableStateOf("") }
    var showReauthDialog by remember { mutableStateOf(false) }

    LaunchedEffect(name, email) {
        nameText = name ?: ""
        emailText = email ?: ""
    }

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

                TextButton(onClick = { /*TODO*/ }) {
                    Text("Change Profile Picture")
                }
            }

            OutlinedTextField(
                value = nameText,
                onValueChange = {nameText = it  },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "First Name") }
            )

            OutlinedTextField(
                value = emailText,
                onValueChange = {emailText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Email") }
            )

            Button(
                onClick = {
                    profileViewModel.updateUserProfile(
                        nameText,
                        emailText,
                        context,
                        onSuccess = {
                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            navController.navigate("profile_screen")
                        },
                        onFailure = { exception ->
                            Toast.makeText(context, "Failed to update profile: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Details")
            }

            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Old Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "New Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Confirm New Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )


            Button(
                onClick = {
                    // Show reauthentication dialog before updating the password
                    showReauthDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Change Password")
            }

            // Reauthentication Dialog
            if (showReauthDialog) {
                var showError by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { showReauthDialog = false },
                    title = { Text("Reauthenticate") },
                    text = {
                        Column {
                            Text("Please re-enter your current password to confirm it's you.")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = reauthPassword,
                                onValueChange = {
                                    reauthPassword = it
                                    showError = it.isEmpty() // Update showError based on whether input is empty
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(text = "Current Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                isError = showError
                            )
                            if (showError) {
                                Text(
                                    "Password cannot be empty",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (reauthPassword.isEmpty()) {
                                    showError = true // Show error if confirm is clicked with empty password
                                } else {
                                    profileViewModel.updateUserPassword(
                                        reauthPassword, newPassword, confirmNewPassword, context,
                                        onSuccess = {
                                            Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmNewPassword = ""
                                            reauthPassword = ""
                                            showReauthDialog = false
                                        },
                                        onFailure = { exception ->
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmNewPassword = ""
                                            reauthPassword = ""
                                            showReauthDialog = false
                                        }
                                    )
                                }
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showReauthDialog = false
                            reauthPassword = ""}) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Button(
                onClick = {
                    // Show reauthentication dialog before updating the password
                   navController.navigate("login_screen")
                },
            ) {
                Text("Log Out")
            }




        }
    }



@Preview(showBackground = true)
@Composable
fun PreviewEditProfileScreen() {
    EditProfileScreen()
}