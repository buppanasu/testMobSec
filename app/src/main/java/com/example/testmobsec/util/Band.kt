package com.example.testmobsec.util

import com.google.firebase.firestore.DocumentReference
// Data class representing the structure of a band entity.
data class Band(
    val bandName: String = "",
    var members: List<String> = emptyList(), // Store member IDs as Strings
    var bandId: String = "",
    var imageUrl: String? = null,
    var followers: List<DocumentReference> = emptyList()
)
