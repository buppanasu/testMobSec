package com.example.testmobsec.viewModel


import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class ProfileViewModel() : ViewModel() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage = FirebaseStorage.getInstance()

    // StateFlow for list of profileImageUrls
    private val _profileImageUrls = MutableStateFlow<Map<String, String?>>(emptyMap())
    val profileImageUrls: StateFlow<Map<String, String?>> = _profileImageUrls
    val userId = auth.currentUser?.uid

    // StateFlow for followed users
    private val _followedUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val followedUsers: StateFlow<List<Map<String, Any>>> = _followedUsers
    var currentUser by mutableStateOf(auth.currentUser)
        private set

    // StateFlow for profileImageUrl
    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl = _profileImageUrl.asStateFlow()

    // StateFlow for name
    private val _name = MutableStateFlow<String?>(null)
    val name = _name.asStateFlow()

    // StateFlow to hold current user role
    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole = _currentUserRole.asStateFlow()

    // StateFlow to hold username
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    // StateFlows to hold follower and following booleans
    private val _following = MutableStateFlow<List<DocumentReference>>(emptyList())
    val following: StateFlow<List<DocumentReference>> = _following
    private val _isFollowing = MutableStateFlow<Boolean?>(null)
    val isFollowing: StateFlow<Boolean?> = _isFollowing

    // StateFlows to hold counts for following and followers
    private val _followersCount = MutableStateFlow<Int>(0)
    val followersCount: StateFlow<Int> = _followersCount
    private val _followingCount = MutableStateFlow<Int>(0)
    val followingCount: StateFlow<Int> = _followingCount


    // StateFlow to hold email
    private val _email = MutableStateFlow<String?>(null)
    val email = _email.asStateFlow()

    // StateFlow to hold list of chat starters
    private val _chatStarters = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val chatStarters: StateFlow<List<Map<String, Any>>> = _chatStarters

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        currentUser = firebaseAuth.currentUser
        // When the auth state changes, load user profile information
        loadUserProfile()
    }

    // fetch a list of users that have started a chat with the band that the current user is a member of
    fun fetchBandChatStarters() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val userBandsSnapshot = firestore.collection("bands")
                    .whereArrayContains("members", currentUserId)
                    .get()
                    .await()

                val bandIds = userBandsSnapshot.documents.mapNotNull { it.id }

                val senderIds = mutableSetOf<String>()
                bandIds.forEach { bandId ->
                    val bandChatsSnapshot = firestore.collection("chat")
                        .whereEqualTo("receiverId", bandId)
                        .get()
                        .await()

                    senderIds.addAll(bandChatsSnapshot.documents.mapNotNull { it.getString("senderId") })
                }

                val chatStarterDetails = senderIds.map { senderId ->
                    Log.d("ChatViewModel",senderId)
                    async {
                        firestore.collection("users").document(senderId).get().await().let { doc ->
                            mapOf(
                                "userId" to senderId,
                                "name" to (doc.getString("name") ?: "Unknown User")
                            )

                        }
                    }
                }.awaitAll()


                _chatStarters.value = chatStarterDetails
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching band chat starters", e)
            }
        }
    }

    // check if the current user is following a specific user based on his/her user ID
    fun checkIfFollowing(targetUserId: String) {
        val currentUserRef =
            auth.currentUser?.uid?.let { firestore.collection("users").document(it) } ?: return

        currentUserRef.get().addOnSuccessListener { document ->
            val following = document["following"] as? List<DocumentReference> ?: listOf()
            _isFollowing.value = following.any { it.id == targetUserId }
        }.addOnFailureListener { e ->
            Log.e("ProfileViewModel", "Error checking follow status", e)
        }
    }

    // Function to toggle the current user's follow status for a specific user by its ID.
    fun toggleFollowUser(targetUserId: String) {
        val currentUserRef = auth.currentUser?.uid?.let { firestore.collection("users").document(it) } ?: return
        val targetUserRef = firestore.collection("users").document(targetUserId)

        _isFollowing.value?.let { alreadyFollowing ->
            if (alreadyFollowing) {
                // if the user is already following, unfollow the target user
                currentUserRef.update("following", FieldValue.arrayRemove(targetUserRef))
                targetUserRef.update("followers", FieldValue.arrayRemove(currentUserRef))
                _isFollowing.value = false
            } else {
                // Follow user if not already following
                currentUserRef.update("following", FieldValue.arrayUnion(targetUserRef))
                targetUserRef.update("followers", FieldValue.arrayUnion(currentUserRef))
                _isFollowing.value = true
            }
        }
    }
    // fetches the current users role
    fun fetchCurrentUserRole() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val role = documentSnapshot.getString("role") ?: "USER" // Default to "USER" if not found
                _currentUserRole.value = role
            }
        }.addOnFailureListener {
            Log.e("ProfileViewModel", "Error fetching user role", it)
        }
    }


    // fetches the user details from who the current user is following
    fun fetchUserDetailsFromFollowing() {
        viewModelScope.launch {
            try {
                val currentUserDocRef = userId?.let { firestore.collection("users").document(it) } ?: return@launch
                val currentUserDoc = currentUserDocRef.get().await()
                val followingRefs = currentUserDoc.get("following") as? List<DocumentReference> ?: listOf()

                val userDetailsList = mutableListOf<Map<String, Any>>()

                // Filter out band references; keep only user references
                val userRefs = followingRefs.filter { it.path.startsWith("users/") }

                // Process in chunks to avoid hitting Firestore's in-query limits
                userRefs.chunked(10).forEach { chunk ->
                    val tasks = chunk.map { userRef ->
                        async {
                            userRef.get().await()
                        }
                    }

                    // Await all tasks and compile the user details
                    tasks.awaitAll().forEach { userSnapshot ->
                        if (userSnapshot.exists()) {
                            val userData = userSnapshot.data?.plus("userId" to userSnapshot.id) ?: mapOf("userId" to userSnapshot.id)
                            userDetailsList.add(userData)
                        }
                    }
                }

                // Update the StateFlow with the new user details
                _followedUsers.value = userDetailsList
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching user details from following", e)
                _followedUsers.value = emptyList()
            }
        }
    }


    // Function to asynchronously fetch the counts of following and followers for a given user ID.
// The function accepts an optional parameter `userIdParam` which can be null.
    fun fetchFollowCounts(userIdParam: String? = null) {
        val userIdToUse = userIdParam ?: auth.currentUser?.uid
        userIdToUse?.let { userId ->
            // Fetch following count
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val following = documentSnapshot["following"] as? List<*>
                    _followingCount.value = following?.size ?: 0

                    // Assuming you store followers in a way that can be counted directly from the user's document
                    val followers = documentSnapshot["followers"] as? List<*>
                    _followersCount.value = followers?.size ?: 0
                }.addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Error fetching follow counts", e)
            }
        }
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }
    // Function to fetch a profile image URL by userId and update the cache
    fun fetchProfileImageUrlByUserId(userId: String) {
        // Check if the URL is already cached to avoid refetching
        if (_profileImageUrls.value.containsKey(userId)) return

        // Define the path in Firebase Storage where the profile image is stored
        val storageRef = storage.reference.child("images/$userId/profile_picture.jpg")

        viewModelScope.launch {
            try {
                val url = storage.reference.child("images/$userId/profile_picture.jpg").downloadUrl.await().toString()
                // Update only the specific entry for efficiency
                _profileImageUrls.value = _profileImageUrls.value.toMutableMap().also { it[userId] = url }
            } catch (e: Exception) {
                // Log the error or handle it as needed. The default image URL is already set.
                Log.e("ProfileViewModel", "Error fetching profile image for userId: $userId", e)
            }
        }
    }


    private fun loadUserProfile() {
        currentUser?.let { user ->
            val userEmail = user.email
            val usersCollection = firestore.collection("users")
            // Assuming 'users' is the collection where user profiles are stored
            CoroutineScope(Dispatchers.IO).launch {
                usersCollection.whereEqualTo("email", userEmail).get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (!documentSnapshot.isEmpty()) {
                            // Use the appropriate field names that you have set in Firestore
                            val documentSnapshot = documentSnapshot.documents[0]
                            _name.value = documentSnapshot.getString("name")
                            _email.value = documentSnapshot.getString("email")
                        }
                    }.addOnFailureListener {
                        // Handle the error here
                    }
            }
        }
    }

    fun getCreationTimestamp(): String? {
        return currentUser?.metadata?.creationTimestamp?.let { timestamp ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }



    // Function to asynchronously fetch the profile image url for a given user ID.
// The function accepts an optional parameter `userIdParam` which can be null.
    fun fetchProfileImageUrl(userIdParam: String? = null) {
        // Use the provided userId if available, otherwise use the current user's userId
        val userId = userIdParam ?: auth.currentUser?.uid ?: return

        // Define the path in Firebase Storage where the profile image is stored
        val storageRef = storage.reference.child("images/$userId/profile_picture.jpg")

        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                // Update the StateFlow with the image URL
                _profileImageUrl.value = uri.toString()
            }
            .addOnFailureListener {
                // Handle any errors, e.g., file doesn't exist
                _profileImageUrl.value = null
            }
    }

    fun fetchUserNameByUserId(userId: String) {
        viewModelScope.launch {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    _userName.value = documentSnapshot.getString("name")
                }
                .addOnFailureListener {
                    // Log the error or set _userName.value to null or a default value
                    _userName.value = "Unknown User"
                }
        }
    }


    fun updateProfilePicture(uri: Uri, context: Context, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "User must be logged in to update profile picture.", Toast.LENGTH_SHORT).show()
            }
            onFailure(Exception("User not logged in"))
            return
        }

        // Define the path in Firebase Storage
        val storageRef = storage.reference.child("images/$userId/profile_picture.jpg")

        // Upload the image
        storageRef.putFile(uri)
            .addOnSuccessListener {
                // Get the download URL
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Here you can optionally update the Firestore document if you keep the image URL there
                    Toast.makeText(context, "Photo updated successfully", Toast.LENGTH_SHORT).show()
                    fetchProfileImageUrl()
                }
            }
            .addOnFailureListener { exception ->
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, exception.message ?: "Upload failed", Toast.LENGTH_SHORT).show()
                }
                onFailure(exception)
            }
    }
    // Updates the user's profile with a new name and email.
    fun updateUserProfile(
        name: String,
        email: String,
        context: Context, // Added context parameter
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Check if a user with the given email exists in the database.
        val usersCollection = FirebaseFirestore.getInstance().collection("users")

        usersCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    // Update user's name and email if user exists.
                    val userDoc = querySnapshot.documents[0]
                    val updates = hashMapOf(
                        "name" to name,
                        "email" to email
                    )

                    usersCollection.document(userDoc.id)
                        .set(updates, SetOptions.merge())
                        .addOnSuccessListener {
                            // Show success message and invoke success callback.
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            // Show error message and invoke failure callback on failure.
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, exception.message ?: "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                            onFailure(exception)
                        }
                } else {
                    // Handle case where no user is found with the provided email.
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "No user found with email: $email", Toast.LENGTH_SHORT).show()
                    }
                    onFailure(Exception("No user found with email: $email"))
                }
            }
            .addOnFailureListener { exception ->
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, exception.message ?: "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                }
                onFailure(exception)
            }
    }

    // Updates the user's password
    fun updateUserPassword(
        oldPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        context: Context, // Added context parameter
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (oldPassword == newPassword) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "The new password must be different from the current password.", Toast.LENGTH_SHORT).show()
            }
            onFailure(Exception("The new password must be different from the current password."))
            return
        }

        if (newPassword != confirmNewPassword) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
            }
            onFailure(Exception("New passwords do not match"))
            return
        }

        user?.let {
            val credential = EmailAuthProvider.getCredential(it.email!!, oldPassword)
            it.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            onSuccess()
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, updateTask.exception?.message ?: "Failed to update password", Toast.LENGTH_SHORT).show()
                            }
                            onFailure(updateTask.exception ?: Exception("Failed to update password"))
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, reauthTask.exception?.message ?: "Re-authentication failed", Toast.LENGTH_SHORT).show()
                    }
                    onFailure(reauthTask.exception ?: Exception("Re-authentication failed"))
                }
            }
        } ?: CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            onFailure(Exception("User not logged in"))
        }
    }





    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }
}
