package com.example.testmobsec.util

// Data class representing the structure of join request.
data class JoinRequest(
    val id: String = "",
    val userId: String = "",
    val bandId: String = "",
    val userName: String = "", // Assuming you store the name of the user in the join request
    // Include other fields as necessary
)
