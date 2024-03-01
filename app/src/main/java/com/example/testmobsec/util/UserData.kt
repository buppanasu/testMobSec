package com.example.testmobsec.util

data class UserData(
    var email: String = "",
    var name: String = "",
    var role: UserRole = UserRole.USER,
    var following: List<String> = emptyList()  // Specify the type for following
)
