package com.example.testmobsec.util

// Data class representing the structure of a chat message entity.
data class Chat(
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = "",
    var senderName: String = "", // Resolved sender name for UI display
    var receiverName: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var documentId: String? = null// Assuming timestamp is a Long
) {
}