package com.example.testmobsec.viewModel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testmobsec.util.Band
import com.example.testmobsec.util.JoinRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BandViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()     // Firebase authentication instance for managing user sessions.
    private val userId = auth.currentUser?.uid // Current user's ID, extracted from the Firebase Auth instance.
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val _allBands = MutableStateFlow<List<Band>>(emptyList())
    val allBands: StateFlow<List<Band>> = _allBands.asStateFlow()

    private val _bandProfileImageUrl = MutableStateFlow<String?>(null)
    val bandProfileImageUrl = _bandProfileImageUrl.asStateFlow()

    private val _bandProfileImageUrls = MutableStateFlow<Map<String, String?>>(emptyMap())
    val bandProfileImageUrls: StateFlow<Map<String, String?>> = _bandProfileImageUrls

    // StateFlow to hold the details of a band
    private val _bandDetails = MutableStateFlow<Band?>(null)
    val bandDetails: StateFlow<Band?> = _bandDetails.asStateFlow()

    //Stateflow to hold member names
    private val _memberNames = MutableStateFlow<List<String>>(emptyList())
    val memberNames: StateFlow<List<String>> = _memberNames.asStateFlow()
    var currentUser by mutableStateOf(auth.currentUser)
        private set

    //TEST
    private val _joinRequests = MutableStateFlow<List<JoinRequest>>(emptyList())
    val joinRequests: StateFlow<List<JoinRequest>> = _joinRequests

    private val _userRole = MutableStateFlow<String>("") // Default to empty string
    val userRole: StateFlow<String> = _userRole


    private val _followedBands = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val followedBands: StateFlow<List<Map<String, Any>>> = _followedBands

    // StateFlows to hold counts
    private val _followersCount = MutableStateFlow<Int>(0)
    val followersCount: StateFlow<Int> = _followersCount

    init {
        fetchUserRole() // Call this when the ViewModel is created
    }


    fun fetchBandFollowerCount(bandIdParam: String? = null) {
        val bandIdToUse = bandIdParam // Assuming bandIdParam is always provided for this function
        bandIdToUse?.let { bandId ->
            firestore.collection("bands").document(bandId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val followers = documentSnapshot["followers"] as? List<*>
                    _followersCount.value = followers?.size ?: 0
                }.addOnFailureListener { e ->
                    Log.e("BandViewModel", "Error fetching band follower count", e)
                }
        }
    }

    // Call this function to fetch the band's profile image URL
    fun fetchBandProfileImageUrl(bandId: String) {
        viewModelScope.launch {
            val storageRef = storage.reference.child("bands/$bandId/profile_picture.jpg")
            try {
                val imageUrl = storageRef.downloadUrl.await().toString()
                _bandProfileImageUrl.value = imageUrl
            } catch (e: Exception) {
                Log.e("BandViewModel", "Error fetching band profile image URL", e)
                _bandProfileImageUrl.value = null // or a default image URL
            }
        }
    }



    // Call this function to update the band's profile picture
    fun updateBandProfilePicture(bandId: String, uri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        viewModelScope.launch {
            val storageRef = storage.reference.child("bands/$bandId/profile_picture.jpg")
            try {
                // Start the upload task
                val uploadTaskSnapshot = storageRef.putFile(uri).await()
                val imageUrl = uploadTaskSnapshot.metadata?.reference?.downloadUrl?.await().toString()

                // Update the Firestore document with the new image URL
                firestore.collection("bands").document(bandId)
                    .update("imageUrl", imageUrl).await()

                // If we reach this point, both the upload and Firestore update succeeded
                onSuccess()
                _bandProfileImageUrl.value = imageUrl
            } catch (e: Exception) {
                Log.e("BandViewModel", "Error updating band profile picture", e)
                onFailure(e)
                _bandProfileImageUrl.value = null
            }
        }
    }


    // fetches all bands
    fun fetchAllBands() {
        viewModelScope.launch {
            try {
                val bandsSnapshot = firestore.collection("bands").get().await()
                val bands = bandsSnapshot.documents.mapNotNull { document ->
                    // Attempting to deserialize directly; if this doesn't work, we'll need to handle 'members' manually.
                    document.toObject(Band::class.java)?.apply {
                        bandId = document.id
                    }
                }
                _allBands.value = bands
            } catch (e:
                     Exception) {
                Log.e("BandViewModel", "Error fetching all bands", e)
                // Consider updating your UI state to indicate an error has occurred
            }
        }
    }

    // fetches bands and their details based on the current user and who he follows
    fun fetchBandsCurrentUserFollows() {
        viewModelScope.launch {
            try {
                // Assuming 'userId' holds the ID of the current user
                val currentUserDocRef = userId?.let { firestore.collection("users").document(it) }
                // Construct the query to find bands where the 'followers' array contains the currentUserDocRef
                val querySnapshot = currentUserDocRef?.let {
                    firestore.collection("bands")
                        .whereArrayContains("followers", it)
                        .get().await()
                }

                val followedBandsWithDetails = mutableListOf<Map<String, Any>>()

                // Iterate through the query results
                if (querySnapshot != null) {
                    for (document in querySnapshot.documents) {
                        val bandData = (document.data as? Map<String, Any> ?: continue).toMutableMap()
                        bandData["bandId"] = document.id                       // Optionally, add the band's document ID to the map

                        followedBandsWithDetails.add(bandData)
                    }
                }

                // Update LiveData or StateFlow with the list of bands the user follows
                _followedBands.value = followedBandsWithDetails
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching bands the current user follows", e)
                _followedBands.value = emptyList()
            }
        }
    }


    // fetch band details based on a specific band by its ID
    fun fetchBandDetails(bandId: String) {
        viewModelScope.launch {
            try {
                val bandSnapshot = firestore.collection("bands").document(bandId).get().await()
                val band = bandSnapshot.toObject(Band::class.java)
                band?.let {
                    _bandDetails.value = it
                    println("Fetched members: ${it.members}")
                }
            } catch (e: Exception) {
                Log.e("BandViewModel", "Error fetching band details", e)
            }
        }
    }


    fun fetchBandMemberNames(members: List<String>) {
        viewModelScope.launch {
            val names = mutableListOf<String>()
            members.forEach { memberId ->
                try {
                    val userSnapshot = firestore.collection("users").document(memberId).get().await()
                    userSnapshot.getString("name")?.let { name ->
                        names.add(name)
                    }
                } catch (e: Exception) {
                    Log.e("BandViewModel", "Error fetching member details", e)
                }
            }
            _memberNames.value = names // Update the StateFlow with the new list
        }
    }

    // fetches join requests based on the specific band by its ID
    fun fetchJoinRequests(bandId: String) {
        viewModelScope.launch {
            firestore.collection("bands").document(bandId)
                .get()
                .addOnSuccessListener { bandSnapshot ->
                    val userIds = bandSnapshot["joinRequests"] as List<String>? ?: emptyList()
                    fetchUserDetails(userIds)
                }
                .addOnFailureListener { e ->
                    // Handle error
                    Log.e("BandViewModel", "Error fetching join requests", e)
                }
        }
    }

    // fetches user details based on a list of userId's given
    private fun fetchUserDetails(userIds: List<String>) {
        userIds.forEach { userId ->
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    val userName = userSnapshot.getString("name") ?: "Unknown"
                    val joinRequest = JoinRequest(userId, userName)
                    _joinRequests.value = _joinRequests.value + listOf(joinRequest)
                }
                .addOnFailureListener { e ->
                    // Handle error
                    Log.e("BandViewModel", "Error fetching user details", e)
                }
        }
    }

    // fetches the current users role
    fun fetchUserRole() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userDocRef.get().addOnSuccessListener { documentSnapshot ->
                _userRole.value = documentSnapshot.getString("role") ?: "USER" // Default to USER if null
            }.addOnFailureListener {
                _userRole.value = "USER" // Default to USER on error
            }
        }
    }

    fun handleJoinRequest(bandId: String, userId: String, accept: Boolean) {
        // Begin a batch write operation
        val batch = firestore.batch()
        val bandRef = firestore.collection("bands").document(bandId)
        if (accept) {
            // Add to members
            batch.update(bandRef, "members", FieldValue.arrayUnion(userId))
        }
        // Remove from joinRequests regardless of accept or reject
        batch.update(bandRef, "joinRequests", FieldValue.arrayRemove(userId))
        // Commit the batch
        batch.commit().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Success
            } else {
                // Handle failure
            }
        }
    }
    private val _isFollowing = MutableStateFlow<Boolean?>(null)
    val isFollowing: StateFlow<Boolean?> = _isFollowing

    // Function to check if the current user is following a specific band by its ID.
    fun checkIfFollowingBand(targetBandId: String) {
        val currentUserRef =
            auth.currentUser?.uid?.let { firestore.collection("users").document(it) } ?: return

        currentUserRef.get().addOnSuccessListener { document ->
            val following = document["following"] as? List<DocumentReference> ?: listOf()
            _isFollowing.value = following.any { it.id == targetBandId }
        }.addOnFailureListener { e ->
            Log.e("ProfileViewModel", "Error checking follow status", e)
        }
    }
    // Function to toggle the current user's follow status for a specific band by its ID.
    fun toggleFollowBand(targetBandId: String) {
        val currentUserRef = auth.currentUser?.uid?.let { firestore.collection("users").document(it) } ?: return


        val bandRef = firestore.collection("bands").document(targetBandId)
        _isFollowing.value?.let { alreadyFollowing ->
            if (alreadyFollowing) {
                // Unfollow
                currentUserRef.update("following", FieldValue.arrayRemove(bandRef))
                bandRef.update("followers", FieldValue.arrayRemove(currentUserRef))
                _isFollowing.value = false
            } else {
                // Follow
                currentUserRef.update("following", FieldValue.arrayUnion(bandRef))
                bandRef.update("followers", FieldValue.arrayUnion(currentUserRef))
                _isFollowing.value = true
            }
        }
    }




}