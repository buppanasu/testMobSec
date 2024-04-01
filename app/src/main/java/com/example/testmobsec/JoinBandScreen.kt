package com.example.testmobsec

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testmobsec.util.Band
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await

@Composable
fun JoinBandScreen(navController: NavController = rememberNavController()) {
    val searchQuery = remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<Band>() }
    val coroutineScope = rememberCoroutineScope()

    // States to handle the display of the band details overlay/dialog
    var showBandDetails by remember { mutableStateOf(false) }
    var selectedBand by remember { mutableStateOf<Band?>(null) }

    // StateFlow to debounce the search input
    val searchQueryFlow = remember { MutableStateFlow("") }

    // Observe the searchQueryFlow and update the search results accordingly
    LaunchedEffect(searchQueryFlow) {
        searchQueryFlow
            .debounce(300) // Debounce the user input to avoid too many Firestore queries
            .collectLatest { query ->
                if (query.isNotBlank()) {
                    // Perform the search query and update the searchResults list
                    val bandsCollectionRef = FirebaseFirestore.getInstance().collection("bands")
                    bandsCollectionRef
                        .orderBy("bandName")
                        .startAt(query)
                        .endAt("$query\uf8ff")
                        .get()
                        .addOnSuccessListener { documents ->
                            searchResults.clear()
                            for (document in documents) {
                                val band = document.toObject(Band::class.java).also {
                                    it.bandId =
                                        document.id // Store the document ID in the band object
                                }
                                searchResults.add(band)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("JoinBandScreen", "Error fetching bands", e)
                        }
                } else {
                    searchResults.clear()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { query ->
                searchQuery.value = query
                searchQueryFlow.value = query // Update the StateFlow with the new query
            },
            label = { Text("Search Bands") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display search results
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(searchResults) { band ->
                BandListItem(band = band) {
                    selectedBand = band
                    showBandDetails = true
                }
            }
        }
    }

    // Show band details dialog
    if (showBandDetails && selectedBand != null) {
        BandDetailsDialog(band = selectedBand!!, onDismiss = { showBandDetails = false }) {
            // User clicked 'Join Band'
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                requestToJoinBand(selectedBand!!.bandId, userId)
                showBandDetails = false
            }
        }
    }
}

@Composable
fun BandDetailsDialog(band: Band, onDismiss: () -> Unit, onJoinClick: () -> Unit) {
    var memberNames by remember { mutableStateOf<List<String>?>(null) }

    // A side effect to fetch member names when the dialog is shown
    LaunchedEffect(band) {
        memberNames = getUserNames(band.members)
    }

    if (memberNames != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Band Details") },
            text = {
                Column {
                    Text("Band Name: ${band.bandName}")
                    Text("Members:")
                    memberNames!!.forEach { name ->
                        Text(name)
                    }
                }
            },
            confirmButton = {
                Button(onClick = onJoinClick) {
                    Text("Join Band")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    } else {
        // Optionally show a progress indicator while loading
        CircularProgressIndicator()
    }
}

@Composable
fun BandListItem(band: Band, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Band Name: ${band.bandName}", style = MaterialTheme.typography.titleMedium)
            // Add more band details if needed
        }
    }
}


// Function to request to join a band
fun requestToJoinBand(bandId: String, userId: String) {
    val bandRef = FirebaseFirestore.getInstance().collection("bands").document(bandId)
    bandRef.update("joinRequests", FieldValue.arrayUnion(userId))
        .addOnSuccessListener {
            // Handle successful join request operation
            Log.d("JoinBandScreen", "Join request sent.")
        }
        .addOnFailureListener { e ->
            // Handle failure to send join request
            Log.e("JoinBandScreen", "Error sending join request", e)
        }
}


suspend fun getUserNames(userIds: List<String>): List<String> {
    val firestore = FirebaseFirestore.getInstance()
    return userIds.mapNotNull { userId ->
        val userDoc = firestore.collection("users").document(userId).get().await()
        userDoc.getString("name")
    }
}