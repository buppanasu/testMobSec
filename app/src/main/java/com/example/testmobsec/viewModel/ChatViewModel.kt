package com.example.testmobsec.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.testmobsec.util.Chat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _userBandId = MutableStateFlow<String?>(null)
    val userBandId: StateFlow<String?> = _userBandId




    init {


        fetchUserRoleAndBand()
//        fetchChats()
        setupChatFetching()


    }

    // Assuming you have a ViewModel or Repository

    private fun setupChatFetching() {
        viewModelScope.launch {
            _userRole.combine(_userBandId) { role, bandId ->
                Pair(role, bandId)
            }.collect { (role, bandId) ->
                when (role) {
                    "ARTIST" -> {
                        if (bandId != null) fetchBandChats(bandId)
                        else Log.d("ChatViewModel", "Artist without a bandId.")
                    }
                    else -> fetchUserChats(receiverId)
                }
            }
        }
    }



    fun fetchUserRoleAndBand() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("ChatViewModel", "Fetching user role and band for userID: $userId")

        viewModelScope.launch {
            try {
                val userSnapshot = db.collection("users").document(userId).get().await()
                _userRole.value = userSnapshot.getString("role")
                Log.d("ChatViewModel", "User role: ${_userRole.value}")

                if (_userRole.value == "ARTIST") {
                    val bandSnapshot = db.collection("bands")
                        .whereArrayContains("members", userId)
                        .get()
                        .await()

                    // Since a member can only be part of one band, take the first result
                    _userBandId.value = bandSnapshot.documents.firstOrNull()?.id
                    Log.d("ChatViewModel", "Band ID for artist: ${_userBandId.value}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching user role and band", e)
            }
        }
    }

    private fun fetchChats() {
        viewModelScope.launch {
            when (_userRole.value) {
                "ARTIST" -> _userBandId.value?.let { bandId ->
                    Log.d("BandID",bandId)
                    fetchBandChats(bandId)
                }
                else -> fetchUserChats(receiverId)
            }
        }
    }

    private fun fetchBandChats(bandId: String) {

        Log.d("ChatViewModel", "Fetching chats for bandId: $bandId with receiverId: $receiverId")

        // Listen to sender's messages
        val senderPath = db.collection("chat")
            .whereEqualTo("senderId", bandId)
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
            .whereEqualTo("receiverId", bandId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.w("ChatViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }
                val chats = snapshot.toObjects(Chat::class.java)
                Log.d("ChatViewModel", "Fetched ${chats.size} chats as receiver.")
                updateChats(snapshot.toObjects(Chat::class.java))
            }
    }

    private fun fetchUserChats(receiverId: String) {
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
                // Automatically determine if the message should be sent as a band based on the user's role
                if (_userRole.value == "ARTIST" && _userBandId.value != null) {
                    val bandId = _userBandId.value!!
                    // Since we're in the artist role, fetch the band's name
                    val bandSnapshot = db.collection("bands").document(bandId).get().await()
                    val bandName = bandSnapshot.getString("bandName") ?: "Unknown Band"

                    // Update the message to be sent from the band's perspective
                    message.senderName = bandName
                    message.senderId = bandId
                }
                // If not an artist, senderName and senderId remain as the user's

                // Send the message
                db.collection("chat").add(message).await()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending chat", e)
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