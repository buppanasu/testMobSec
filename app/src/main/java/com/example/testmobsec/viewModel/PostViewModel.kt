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
import kotlinx.coroutines.flow.asStateFlow
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

    private val _comments = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val comments: StateFlow<List<Map<String, Any>>> = _comments
    val likedPosts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val commentedPosts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    // A map to hold the MutableStateFlows for likes count, keyed by postId
    private val likesCounts = mutableMapOf<String, MutableStateFlow<Int>>()
    private val commentCounts = mutableMapOf<String, MutableStateFlow<Int>>()
    private val _postsCount = MutableStateFlow(0)
    val postsCount = _postsCount.asStateFlow()

    private val _selectedPostDetails = MutableStateFlow<Map<String, Any>?>(null)
    val selectedPostDetails = _selectedPostDetails.asStateFlow()
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

    fun fetchPostsFromFollowing() {
        viewModelScope.launch {
            try {
                val currentUserDocRef = userId?.let { db.collection("users").document(it) } ?: return@launch
                val currentUserDoc = currentUserDocRef.get().await()
                val followingRefs = currentUserDoc.get("following") as? List<DocumentReference> ?: listOf()

                val postsWithUserNames = mutableListOf<Map<String, Any>>()

                // Process in chunks due to Firestore's limitations
                followingRefs.chunked(10).forEach { chunk ->
                    // Construct a list of tasks to fetch posts for each chunk
                    val tasks = chunk.map { userRef ->
                        db.collection("posts")
                            .whereEqualTo("userId", userRef)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .get()
                    }

                    // Await all tasks and process results
                    tasks.forEach { task ->
                        val postsQuerySnapshot = task.await()
                        for (document in postsQuerySnapshot.documents) {
                            val postData = document.data as? MutableMap<String, Any> ?: continue
                            postData["postId"] = document.id

                            val userDoc = (postData["userId"] as? DocumentReference)?.get()?.await()
                            val userName = userDoc?.getString("name") ?: "Unknown User"

                            // Add the user's name to the post data
                            postData["userName"] = userName

                            // Add the modified post data to the list
                            postsWithUserNames.add(postData)
                        }
                    }
                }

                // Update LiveData or StateFlow with the new posts
                _posts.value = postsWithUserNames
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching posts from following", e)
                _posts.value = emptyList()
            }
        }
    }




    fun fetchPostsCountForCurrentUser(userIdParam: String? = null) {

        // Use the provided userId if available, or fall back to the current user's ID
        val userId = userIdParam ?: FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Directly use the userId to query the posts collection
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("posts")
                .whereEqualTo("userId", FirebaseFirestore.getInstance().collection("users").document(userId))
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Update the posts count
                    _postsCount.value = querySnapshot.size()
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                    println("Error getting posts count: $exception")
                    _postsCount.value = 0 // Optionally reset the count or handle the error as needed
                }
        }
    }

    fun fetchCommentedPosts(userIdParam: String? = null) {
        viewModelScope.launch {
            val userDocumentRef = userIdParam?.let { db.collection("users").document(it) }
                ?: userId?.let { db.collection("users").document(it) }
                ?: return@launch  // Return if no userId is found
            val commentPostIds = db.collection("comments")
                .whereEqualTo("userId",userDocumentRef)
                .get()
                .await()
                .documents
                .mapNotNull { it["postId"] as? DocumentReference } // Assuming postId is stored as a DocumentReference

            val postsWithUserNames = commentPostIds.mapNotNull { postIdRef ->
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

            commentedPosts.value = postsWithUserNames
        }
    }
    fun fetchLikedPosts(userIdParam: String? = null) {
        viewModelScope.launch {
            val userDocumentRef = userIdParam?.let { db.collection("users").document(it) }
                ?: userId?.let { db.collection("users").document(it) }
                ?: return@launch  // Return if no userId is found
            val likedPostIds = db.collection("likes")
                .whereEqualTo("userId", userDocumentRef)
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

    fun hasUserCommented(postId: String): Flow<Boolean> = flow {

        val commentsCollection = db.collection("comments")
        val querySnapshot = commentsCollection
            .whereEqualTo("postId", db.collection("posts").document(postId))
            .whereEqualTo("userId", userId?.let { db.collection("users").document(it) })
            .get()
            .await()

        emit(querySnapshot.documents.isNotEmpty())
    }.flowOn(Dispatchers.IO)


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

    fun getCommentsCountFlow(postId: String): MutableStateFlow<Int> {
        // Return an existing flow if one already exists for this postId
        if (commentCounts.containsKey(postId)) {
            return commentCounts[postId]!!
        }

        // Otherwise, create a new MutableStateFlow for this postId, initialize it with 0
        val newCommentsCountFlow = MutableStateFlow(0)
        commentCounts[postId] = newCommentsCountFlow

        // Set up a snapshot listener for this postId
        db.collection("comments")
            .whereEqualTo("postId", db.collection("posts").document(postId))
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("PostViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val commentsCount = snapshot?.size() ?: 0
                // Update the MutableStateFlow with the new count
                newCommentsCountFlow.value = commentsCount
            }

        return newCommentsCountFlow
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
    fun fetchPostByPostId(postId: String) {
        viewModelScope.launch {
            try {
                // Fetch the post document by its postId
                val postSnapshot = db.collection("posts").document(postId).get().await()

                if (!postSnapshot.exists()) {
                    Log.d("PostViewModel", "No post found for postId: $postId")
                    _selectedPostDetails.value = null // Assuming you have a similar MutableStateFlow for storing post details
                    return@launch
                }

                // Assuming the post data structure includes a userId field that is a DocumentReference
                val postData = postSnapshot.data as MutableMap<String, Any> // Make sure to safely cast and handle potential null
                val userIdRef = postData["userId"] as? DocumentReference

                if (userIdRef != null) {
                    // Fetch the user document based on the userId reference
                    val userSnapshot = userIdRef.get().await()
                    val userName = userSnapshot.getString("name") ?: "Unknown User"

                    // Add the user's name to the post data
                    postData["userName"] = userName

                    // Update the MutableStateFlow with the modified post data
                    _selectedPostDetails.value = postData
                } else {
                    Log.d("PostViewModel", "UserId reference missing in post data for postId: $postId")
                    _selectedPostDetails.value = postData // Store the post data even if the username cannot be fetched
                }
            } catch (e: Exception) {
                // Handle error
                Log.e("PostViewModel", "Error fetching post details: ", e)
                _selectedPostDetails.value = null
            }
        }
    }


    fun fetchCommentsForPost(postId: String){
        viewModelScope.launch {
            try {
                // Query the posts collection for documents where the userId field matches the userRef
                val result: QuerySnapshot = db.collection("comments")
                    .whereEqualTo("postId", db.collection("posts").document(postId))
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                if (result.isEmpty) {
                    Log.d("PostViewModel", "No comments found for user")
                } else {
                    Log.d("PostViewModel", "Fetched ${result.size()} comments for user")
                }

                // Initialize an empty list to hold the posts with user names
                val commentsWithUserNames = mutableListOf<Map<String, Any>>()

                // Iterate through each document in the result
                for (document in result.documents) {
                    val commentData = document.data as MutableMap<String, Any>
//                    commentData["postId"] = document.id
                    // Get the user reference from the post
                    val userRef = commentData["userId"] as DocumentReference


                    // Fetch the user document based on the reference
                    val userDoc = userRef.get().await()

                    // Retrieve the user's name from the user document
                    val userName = userDoc.getString("name") ?: "Unknown User"

                    // Add the user's name to the post data
                    commentData["userName"] = userName


                    // Add the modified post data to the list
                    commentsWithUserNames.add(commentData)
                }

                // Update the MutableStateFlow with the modified list of posts
                _comments.value = commentsWithUserNames
            } catch (e: Exception) {
                // Handle error
                Log.e("PostViewModel", "Error fetching comments with user names", e)
                _comments.value = emptyList()
            }
        }

    }



    fun fetchPostsForUser(userIdParam: String? = null) {
        viewModelScope.launch {
            try {
                val userIdToUse = userIdParam ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                // Query the posts collection for documents where the userId field matches the userRef
                val result: QuerySnapshot = db.collection("posts")
                    .whereEqualTo("userId", userId?.let { db.collection("users").document(userIdToUse) })
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

    fun addCommentToPost(postId: String, commentText: String) {

        val commentData = hashMapOf(
            "userId" to FirebaseFirestore.getInstance().document("users/$userId"),
            "postId" to FirebaseFirestore.getInstance().document("posts/$postId"),
            "timestamp" to FieldValue.serverTimestamp(),
            "comment" to commentText
        )

        FirebaseFirestore.getInstance().collection("comments").add(commentData)
            .addOnSuccessListener { Log.d("PostViewModel", "Comment added with ID: ${it.id}") }
            .addOnFailureListener { e -> Log.w("PostViewModel", "Error adding comment", e) }
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




