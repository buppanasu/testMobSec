package com.example.testmobsec.util

import com.google.firebase.firestore.DocumentReference

data class UserData(
    var email: String = "",
    var name: String = "",
    var role: UserRole = UserRole.USER,
    var following: List<DocumentReference> = emptyList(),  // Specify the type for following
    var followers: List<DocumentReference> = emptyList()
)
