package com.example.testmobsec.util

import com.google.firebase.firestore.DocumentReference
data class Band(
    val bandName: String = "",
    var members: List<String> = emptyList(), // Store member IDs as Strings
    var bandId: String = "",
    var imageUrl: String? = null
)
