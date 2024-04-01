package com.example.testmobsec

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBandScreen(navController: NavController = rememberNavController()) {
    // Mutable states for managing the UI input for band name, dropdown menu expansion, and selected band role.
    var bandName: String by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var selectedBandRole by remember { mutableStateOf<BandRole?>(null) }
    val context = LocalContext.current

    // Column layout for centering the input fields on the screen.
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TextField for entering the band's name.
        TextField(
            value = bandName,
            onValueChange = { bandName = it },
            placeholder = { Text(text = "Band Name") }
        )
        Spacer(modifier = Modifier.height(10.dp)) // Spacer for adding space between the text fields.

        // Box for the dropdown menu to select the band role.
        Box(contentAlignment = Alignment.Center) {
            ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }) {
                TextField(
                    value = selectedBandRole?.name ?: "",
                    onValueChange = {},
                    readOnly = true, // Makes the text field read-only.
                    trailingIcon = { TrailingIcon(expanded = isExpanded) }, // Dropdown icon.
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(),
                    placeholder = { Text(text = "Select your role in the band") }
                )
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
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

        // Button for submitting the form to create a new band.
        Button(onClick = {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && selectedBandRole != null && bandName.isNotBlank()) {
                // Firebase Firestore references and data preparation.
                val userId = currentUser.uid
                val bandsCollectionRef = FirebaseFirestore.getInstance().collection("bands")
                val newBandData = hashMapOf(
                    "bandName" to bandName,
                    "members" to listOf(userId) // Initially, the user is the only member.
                )

                // Attempt to add the new band to Firestore.
                bandsCollectionRef.add(newBandData).addOnSuccessListener { documentReference ->
                    val newBandId = documentReference.id
                    // Updates to link the new band with the user's profile.
                    val userUpdates = hashMapOf(
                        "bandId" to newBandId,
                        "bandName" to bandName,
                        "bandRole" to selectedBandRole!!.name,
                        "isBandCreator" to true
                    )

                    val userRef =
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                    userRef.update(userUpdates as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Band created and linked to your profile!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Optionally navigate to the next screen after success.
                        }
                }
                    .addOnFailureListener { e ->
                        Log.e("CreateBandScreen", "Error linking band to user profile", e)
                        Toast.makeText(
                            context,
                            "Failed to link band to your profile.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }.addOnFailureListener { e ->
                Log.e("CreateBandScreen", "Error creating band", e)
                Toast.makeText(context, "Failed to create band.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Validation feedback for incomplete form
            Toast.makeText(
                context,
                "Please enter a band name and select a role.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }) {
        Text("Create Band")
    }
}
}