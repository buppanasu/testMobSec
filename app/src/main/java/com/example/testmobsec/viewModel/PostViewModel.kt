package com.example.testmobsec.viewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
class PostViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private val _posts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val posts: StateFlow<List<Map<String, Any>>> = _posts

    fun fetchPostsForUser() {
        viewModelScope.launch {
            try {
                // Construct a reference to the user's document in the users collection
                val userRef = userId?.let { db.collection("users").document(it) }

                // Query the posts collection for documents where the userId field matches the userRef
                val result: QuerySnapshot = db.collection("posts")
                    .whereEqualTo("userId", userRef) // Use the reference for querying
                    .orderBy("timestamp", Query.Direction.DESCENDING) // Assuming you want to order by the timestamp
                    .get()
                    .await()
                // Check if the result is empty
                if (result.isEmpty) {
                    Log.d("PostViewModel", "No posts found for user")
                } else {
                    Log.d("PostViewModel", "Fetched ${result.size()} posts for user")
                }
                // Mapping the result to a list of maps (or you could use a data class for your posts)
                val postsList = result.documents.mapNotNull { it.data }

                _posts.value = postsList
            } catch (e: Exception) {
                // Handle error
                _posts.value = emptyList()
            }
        }
    }
    fun uploadPost(
        content: String,
        context: Context, // Added context parameter
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {


        val postsCollection = db.collection("posts")


        if (userId == null) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "User must be logged in to create a post.", Toast.LENGTH_SHORT).show()
            }
            onFailure(Exception("User not logged in"))
            return
        }

        // Create a reference to the user's document in the users collection
        val userRef = db.collection("users").document(userId)

        // Create a map to represent the post, using the user document reference for the userId
        val postMap = hashMapOf(
            "userId" to userRef, // Use this instead of "userId" to store a reference
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                postsCollection.add(postMap).await()
                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess()
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onFailure(e)
                }
            }
        }
    }
}




