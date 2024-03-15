package com.example.testmobsec.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.testmobsec.util.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel(private val receiverId: String) : ViewModel() {
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    private val _currentName = MutableStateFlow<String?>(null)
    val currentName: StateFlow<String?> = _currentName


    init {


        fetchChats(receiverId)

    }


    private fun fetchChats(receiverId: String) {
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Listen to sender's messages
        val senderPath = db.collection("chat")
            .whereEqualTo("senderId", senderId)
            .whereEqualTo("receiverId", receiverId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.w("ChatViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }
                updateChats(snapshot.toObjects(Chat::class.java))
            }

        // Listen to receiver's messages
        val receiverPath = db.collection("chat")
            .whereEqualTo("senderId", receiverId)
            .whereEqualTo("receiverId", senderId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.w("ChatViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }
                updateChats(snapshot.toObjects(Chat::class.java))
            }
    }

    private fun updateChats(newChats: List<Chat>) {
        val currentChats = _chats.value.toMutableList()
        // Here, you'd merge newChats into currentChats in a way that avoids duplicates and sorts them.
        // This is a simplified example. You'd need to ensure that this merging logic correctly handles duplicates and maintains order.
        currentChats.addAll(newChats)
        currentChats.sortBy { it.timestamp }
        _chats.value = currentChats.distinctBy { it.timestamp }
    }



    fun fetchCurrentUserName() {
        viewModelScope.launch {
            if (userId != null) {
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { documentSnapshot ->
                        _currentName.value = documentSnapshot.getString("name")
                    }
                    .addOnFailureListener {
                        // Log the error or set _userName.value to null or a default value
                        _currentName.value = "Unknown User"
                    }
            }
        }
    }


    fun fetchNameById(id: String) {
        viewModelScope.launch {
            val userName = fetchNameFromUsersOrBands(id)
            _userName.value = userName
        }
    }

    private suspend fun fetchNameFromUsersOrBands(id: String): String {
        // First, try fetching from the users collection
        val userDocSnapshot = db.collection("users").document(id).get().await()
        if (userDocSnapshot.exists()) {
            return userDocSnapshot.getString("name") ?: "Unknown User"
        }

        // If not found in users, then try fetching from the bands collection
        val bandDocSnapshot = db.collection("bands").document(id).get().await()
        if (bandDocSnapshot.exists()) {
            return bandDocSnapshot.getString("bandName") ?: "Unknown Band"
        }

        // If not found in both, return a default value
        return "Unknown"
    }


    fun sendChat(message: Chat) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("chat").add(message).await()

                // You could update the UI state here to indicate the message was sent successfully
            } catch (e: Exception) {
                // Handle error: update UI state to show error message, log error, etc.
            }
        }
    }
}





class ChatViewModelFactory(private val receiverId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(receiverId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}