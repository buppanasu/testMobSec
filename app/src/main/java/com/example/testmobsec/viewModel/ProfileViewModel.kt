package com.example.testmobsec.viewModel


import android.content.Context
import android.net.Uri
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

class ProfileViewModel : ViewModel() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage = FirebaseStorage.getInstance()

    var currentUser by mutableStateOf(auth.currentUser)
        private set

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl = _profileImageUrl.asStateFlow()

    // StateFlows for first name and last name
    private val _name = MutableStateFlow<String?>(null)
    val name = _name.asStateFlow()



    private val _email = MutableStateFlow<String?>(null)
    val email = _email.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        currentUser = firebaseAuth.currentUser
        // When the auth state changes, load user profile information
        loadUserProfile()
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
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



    fun fetchProfileImageUrl() {
        val userId = auth.currentUser?.uid ?: return

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
    fun updateUserProfile(
        name: String,
        email: String,
        context: Context, // Added context parameter
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val usersCollection = FirebaseFirestore.getInstance().collection("users")

        usersCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val userDoc = querySnapshot.documents[0]
                    val updates = hashMapOf(
                        "name" to name,
                        "email" to email
                    )

                    usersCollection.document(userDoc.id)
                        .set(updates, SetOptions.merge())
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, exception.message ?: "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                            onFailure(exception)
                        }
                } else {
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
