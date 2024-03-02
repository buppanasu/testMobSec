package com.example.testmobsec

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testmobsec.util.BandRole
import com.example.testmobsec.util.UserData
import com.example.testmobsec.util.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBandScreen(navController: NavController = rememberNavController()) {
    var bandName: String by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    var selectedBandRole by remember { mutableStateOf<BandRole?>(null) }

    // Get the current context
    val context = LocalContext.current



    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TextField(
            value = bandName,
            onValueChange = { bandName = it },
            placeholder = { Text(text = "Band Name") },
            //label = {Text(text = "Come up with a band name")},
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(contentAlignment = Alignment.Center) {
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it }
            ) {
                TextField(
                    value = selectedBandRole?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { TrailingIcon(expanded = isExpanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(),
                    placeholder = { Text(text = "Select your role in the band") },

                    )

                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }) {
                    BandRole.values().forEach { role ->
                        DropdownMenuItem(
                            text = { Text(text = role.name.replace('_', ' ')) },
                            onClick = {
                                selectedBandRole = role
                                isExpanded = false
                            }
                        )
                    }


                }


            }
        }

        //Button to update the Band and Role fields for a document(documentID is the firebaseauth user ID
        Button(onClick = {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && selectedBandRole != null && bandName.isNotBlank()) {
                val userId = currentUser.uid // Get the user ID from FirebaseAuth
                val userData = hashMapOf(
                    "bandName" to bandName,
                    "bandRole" to selectedBandRole!!.name
                )

                // Update the user's document with the new fields
                val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                userRef.update(userData as Map<String, Any>)
                    .addOnSuccessListener {
                        // Success handling
                        Toast.makeText(
                            context,
                            "Band and Role updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Navigate to the next screen if needed
                    }
                    .addOnFailureListener { e ->
                        // Error handling
                        Log.e("CreateBandScreen", "Error updating user data", e)
                        Toast.makeText(
                            context,
                            "Failed to update Band and Role.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                val bandRef = FirebaseFirestore.getInstance().collection("bands").document(bandName)
            } else {
                // Handle the case where the band role isn't selected or band name is blank
                Toast.makeText(
                    context,
                    "Please enter a band name and select a role.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }) {
            Text("Register")
        }


    }
}


