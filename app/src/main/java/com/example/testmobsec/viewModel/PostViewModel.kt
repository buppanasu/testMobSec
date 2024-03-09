package com.example.testmobsec.viewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.sql.Timestamp

class PostViewModel: ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private val _posts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val posts: StateFlow<List<Map<String, Any>>> = _posts
    val likedPosts = MutableStateFlow<List<Map<String, Any>>>(emptyList())

    fun fetchPostsForHome(){
        viewModelScope.launch{
            try {
                // Assuming "userId" holds the ID of the current user
                val currentUserRef = userId?.let { db.collection("users").document(it) }

                // Fetch all posts
                val result: QuerySnapshot = db.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING) // Order by the timestamp
                    .get()
                    .await()

                if (result.isEmpty) {
                    Log.d("PostViewModel", "No posts found")
                } else {
                    Log.d("PostViewModel", "Fetched posts excluding current user")
                }

                // Initialize an empty list to hold the posts with user names
                val postsWithUserNames = mutableListOf<Map<String, Any>>()
                val filteredPosts = result.documents.filter { document ->
                    document["userId"] != currentUserRef
                }
                // Iterate through each document in the result
                for (document in filteredPosts) {
                    val postData = document.data as MutableMap<String, Any>
                    postData["postId"] = document.id
                    // Get the user reference from the post
                    val userRef = postData["userId"] as DocumentReference

                    // Fetch the user document based on the reference
                    val userDoc = userRef.get().await()

                    // Retrieve the user's name from the user document
                    val userName = userDoc.getString("name") ?: "Unknown User"

                    // Add the user's name to the post data
                    postData["userName"] = userName

                    // Add the modified post data to the list
                    postsWithUserNames.add(postData)
                }


                _posts.value = postsWithUserNames
            } catch (e: Exception) {
                // Handle error
                _posts.value = emptyList()
            }
        }

    }
    fun fetchLikedPosts() {
        viewModelScope.launch {
            val likedPostIds = db.collection("likes")
                .whereEqualTo("userId", userId?.let { db.collection("users").document(it) })
                .get()
                .await()
                .documents
                .mapNotNull { it["postId"] as? DocumentReference } // Assuming postId is stored as a DocumentReference

            val postsWithUserNames = likedPostIds.mapNotNull { postIdRef ->
                val postSnapshot = postIdRef.get().await()
                if (!postSnapshot.exists()) return@mapNotNull null
                val postData = postSnapshot.data?.plus("postId" to postIdRef.id) ?: return@mapNotNull null

                // Fetch the user's name based on userId in postData
                val userRef = postData["userId"] as? DocumentReference
                val userSnapshot = userRef?.get()?.await()
                val userName = userSnapshot?.getString("name") ?: "Unknown User"

                // Add the userName to postData
                postData.plus("userName" to userName)
            }.sortedByDescending { it["timestamp"] as? Timestamp } // Sort by timestamp descending

            likedPosts.value = postsWithUserNames
        }
    }
    fun isPostLikedByUser(postId: String): Flow<Boolean> = flow {

        val likesCollection = db.collection("likes")
        val querySnapshot = likesCollection
            .whereEqualTo("postId", db.collection("posts").document(postId))
            .whereEqualTo("userId", userId?.let { db.collection("users").document(it) })
            .get()
            .await()

        emit(querySnapshot.documents.isNotEmpty())
    }.flowOn(Dispatchers.IO)

    // A map to hold the MutableStateFlows for likes count, keyed by postId
    private val likesCounts = mutableMapOf<String, MutableStateFlow<Int>>()

    fun getLikesCountFlow(postId: String): MutableStateFlow<Int> {
        // Return an existing flow if one already exists for this postId
        if (likesCounts.containsKey(postId)) {
            return likesCounts[postId]!!
        }

        // Otherwise, create a new MutableStateFlow for this postId, initialize it with 0
        val newLikesCountFlow = MutableStateFlow(0)
        likesCounts[postId] = newLikesCountFlow

        // Set up a snapshot listener for this postId
        db.collection("likes")
            .whereEqualTo("postId", db.collection("posts").document(postId))
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PostViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val likesCount = snapshot?.size() ?: 0
                // Update the MutableStateFlow with the new count
                newLikesCountFlow.value = likesCount
            }

        return newLikesCountFlow
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val likesCollection = db.collection("likes")
            val query = likesCollection
                .whereEqualTo("postId", db.collection("posts").document(postId))
                .whereEqualTo("userId", userId?.let { db.collection("users").document(it) })
                .get()
                .await()

            if (query.documents.isNotEmpty()) {
                // User already liked the post, so remove the like
                query.documents.first().reference.delete().await()
            } else {
                // User hasn't liked the post, so add a new like
                val likeMap = hashMapOf(
                    "postId" to db.collection("posts").document(postId),
                    "userId" to userId?.let { db.collection("users").document(it) },
                    "timestamp" to FieldValue.serverTimestamp()
                )
                likesCollection.add(likeMap).await()
            }
        }
    }

    fun fetchLikesCountForPost(postId: String): Flow<Int> = flow {
        try {
            val likesCount = db.collection("likes")
                .whereEqualTo("postId", db.collection("posts").document(postId))
                .get()
                .await()
                .size()
            emit(likesCount)
        } catch (e: Exception) {
            Log.e("PostViewModel", "Error fetching likes count", e)
            emit(0) // Emit 0 if there's an error
        }
    }.flowOn(Dispatchers.IO)


    fun likePost(postId: String, userId: String) {
        viewModelScope.launch {
            val likeMap = hashMapOf(
                "postId" to db.collection("posts").document(postId),
                "userId" to db.collection("users").document(userId),
                "timestamp" to FieldValue.serverTimestamp()
            )

            try {
                db.collection("likes").add(likeMap).await()
                Log.d("PostViewModel", "Like added successfully")
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error adding like", e)
            }
        }
    }


    fun fetchPostsForUser() {
        viewModelScope.launch {
            try {
                // Query the posts collection for documents where the userId field matches the userRef
                val result: QuerySnapshot = db.collection("posts")
                    .whereEqualTo("userId", userId?.let { db.collection("users").document(it) })
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                if (result.isEmpty) {
                    Log.d("PostViewModel", "No posts found for user")
                } else {
                    Log.d("PostViewModel", "Fetched ${result.size()} posts for user")
                }

                // Initialize an empty list to hold the posts with user names
                val postsWithUserNames = mutableListOf<Map<String, Any>>()

                // Iterate through each document in the result
                for (document in result.documents) {
                    val postData = document.data as MutableMap<String, Any>
                    postData["postId"] = document.id
                    // Get the user reference from the post
                    val userRef = postData["userId"] as DocumentReference

                    // Fetch the user document based on the reference
                    val userDoc = userRef.get().await()

                    // Retrieve the user's name from the user document
                    val userName = userDoc.getString("name") ?: "Unknown User"

                    // Add the user's name to the post data
                    postData["userName"] = userName

                    // Add the modified post data to the list
                    postsWithUserNames.add(postData)
                }

                // Update the MutableStateFlow with the modified list of posts
                _posts.value = postsWithUserNames
            } catch (e: Exception) {
                // Handle error
                Log.e("PostViewModel", "Error fetching posts with user names", e)
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




