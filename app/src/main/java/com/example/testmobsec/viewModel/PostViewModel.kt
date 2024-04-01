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
// ViewModel responsible for handling creation of posts,feedback and comments
class PostViewModel: ViewModel() {
    // Firebase Firestore and Auth instances for database and authentication operations.
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId = auth.currentUser?.uid // The current user's ID, if logged in.

    // StateFlows for various data types to be observed by the UI.
    private val _posts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val posts: StateFlow<List<Map<String, Any>>> = _posts
    // Similar structure is used for posts from bands, followed band posts, and band feedbacks.
    private val _bandPosts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val bandPosts: StateFlow<List<Map<String, Any>>> = _bandPosts

    private val _followedBandPosts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val followedBandPosts: StateFlow<List<Map<String, Any>>> = _followedBandPosts

    private val _bandFeedbacks = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val bandFeedbacks: StateFlow<List<Map<String, Any>>> = _bandFeedbacks

    private val _comments = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val comments: StateFlow<List<Map<String, Any>>> = _comments
    val likedPosts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val commentedPosts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    // A map to hold the MutableStateFlows for likes count, keyed by postId
    private val likesCounts = mutableMapOf<String, MutableStateFlow<Int>>()
    private val commentCounts = mutableMapOf<String, MutableStateFlow<Int>>()
    private val _postsCount = MutableStateFlow(0)
    private val _bandPostsCount = MutableStateFlow(0)
    val postsCount = _postsCount.asStateFlow()
    val bandPostsCount = _bandPostsCount.asStateFlow()

    private val _selectedPostDetails = MutableStateFlow<Map<String, Any>?>(null)
    val selectedPostDetails = _selectedPostDetails.asStateFlow()

    // Fetches posts for the home screen, including posts from bands and followed users.
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
                // Filter posts that do not have a "bandId" field and are not associated with the currentUserRef
                val filteredPosts = result.documents.filter { document ->
                    val hasNoBandId = document["bandId"] == null
                    document["userId"] != currentUserRef && hasNoBandId
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

    // Fetches posts from bands the current user follows.
    fun fetchPostsForFollowedBands() {
        viewModelScope.launch {
            try {
                val currentUserDocRef = FirebaseAuth.getInstance().currentUser?.uid?.let {
                    db.collection("users").document(it)
                } ?: return@launch

                // First, fetch the list of bands the user follows by checking the followers field
                val followedBandsQuerySnapshot = db.collection("bands")
                    .whereArrayContains("followers", currentUserDocRef)
                    .get()
                    .await()

                val followedBandRefs = followedBandsQuerySnapshot.documents.map { it.reference }

                val postsWithBandNames = mutableListOf<Map<String, Any>>()

                // Process in chunks due to Firestore's limitations
                followedBandRefs.chunked(10).forEach { chunk ->
                    val tasks = chunk.map { bandRef ->
                        db.collection("posts")
                            .whereEqualTo("bandId", bandRef)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .get()
                    }

                    // Await all tasks and process results
                    tasks.forEach { task ->
                        val postsQuerySnapshot = task.await()
                        for (document in postsQuerySnapshot.documents) {
                            val postData = document.data as? MutableMap<String, Any> ?: continue
                            postData["postId"] = document.id

                            val bandRef = postData["bandId"] as DocumentReference

                            // Fetch the user document based on the reference
                            val bandDoc = bandRef.get().await()

                            // Retrieve the user's name from the user document
                            val bandName = bandDoc.getString("bandName") ?: "Unknown User"

                            val bandImageUrl = bandDoc.getString("imageUrl") ?: "Unknown User"


                            // Add the user's name to the post data
                            postData["bandName"] = bandName
                            postData["imageUrl"] = bandImageUrl
                            postsWithBandNames.add(postData)
                        }
                    }
                }

                // Update LiveData or StateFlow with the new posts
                _followedBandPosts.value = postsWithBandNames
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching posts from followed bands", e)
                _followedBandPosts.value = emptyList()
            }
        }
    }

    // Fetches posts made by users the current user is following.
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


    // Fetches the count of posts made by the current user or specified user
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

    // Function to asynchronously fetch the count of posts made by a specified band.
    // The function accepts an optional parameter `bandIdParam` which can be null.
    fun fetchPostsCountForBand(bandIdParam: String? = null) {
        // Use the provided bandId if available
        val bandId = bandIdParam ?: return

        // Directly use the bandId to query the posts collection
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("posts")
                .whereEqualTo("bandId", FirebaseFirestore.getInstance().collection("bands").document(bandId))
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Update the posts count for the band
                    _bandPostsCount.value = querySnapshot.size()
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                    println("Error getting band posts count: $exception")
                    _bandPostsCount.value = 0 // Optionally reset the count or handle the error as needed
                }
        }
    }

    // Function to asynchronously fetch the commented posts the user has commented on.
    // The function accepts an optional parameter `userIdParam` which can be null.
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

    // Function to asynchronously fetch the posts user has liked.
    // The function accepts an optional parameter `userIdParam` which can be null.
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
    // check to see if the current user has liked a post based on its post ID
    fun isPostLikedByUser(postId: String): Flow<Boolean> = flow {

        val likesCollection = db.collection("likes")
        val querySnapshot = likesCollection
            .whereEqualTo("postId", db.collection("posts").document(postId))
            .whereEqualTo("userId", userId?.let { db.collection("users").document(it) })
            .get()
            .await()

        emit(querySnapshot.documents.isNotEmpty())
    }.flowOn(Dispatchers.IO)

    // check to see if the current user has left a comment on a post based on its post ID
    fun hasUserCommented(postId: String): Flow<Boolean> = flow {

        val commentsCollection = db.collection("comments")
        val querySnapshot = commentsCollection
            .whereEqualTo("postId", db.collection("posts").document(postId))
            .whereEqualTo("userId", userId?.let { db.collection("users").document(it) })
            .get()
            .await()

        emit(querySnapshot.documents.isNotEmpty())
    }.flowOn(Dispatchers.IO)

    // get likes count for each specific post based on its post ID
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
    // get the comments count for each post based on its post ID
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

    // Toggles the like status of a post for the current user.
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

    // fetch a post by its post ID
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

    // fetch comments for a specific post based on its post ID
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

    fun fetchPostsForBand(bandIdParam: String? = null) {
        viewModelScope.launch {
            try {
                val bandIdToUse = bandIdParam ?: return@launch
                val bandRef = db.collection("bands").document(bandIdToUse)

                // Query the posts collection for documents where the bandId field matches the bandRef
                val result: QuerySnapshot = db.collection("posts")
                    .whereEqualTo("bandId", bandRef) // Use bandRef directly
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                if (result.isEmpty) {
                    Log.d("PostViewModel", "No posts found for user")
                } else {
                    Log.d("PostViewModel", "Fetched ${result.size()} posts for user")
                }

                // Initialize an empty list to hold the posts with user names
                val postsWithBandNames = mutableListOf<Map<String, Any>>()

                // Iterate through each document in the result
                for (document in result.documents) {
                    val postData = document.data as MutableMap<String, Any>
                    postData["postId"] = document.id
                    // Get the user reference from the post
                    val bandRef = postData["bandId"] as DocumentReference

                    // Fetch the user document based on the reference
                    val bandDoc = bandRef.get().await()

                    // Retrieve the user's name from the user document
                    val bandName = bandDoc.getString("bandName") ?: "Unknown User"

                    // Add the user's name to the post data
                    postData["bandName"] = bandName

                    // Add the modified post data to the list
                    postsWithBandNames.add(postData)
                }

                // Update the MutableStateFlow with the modified list of posts
                _bandPosts.value = postsWithBandNames
            } catch (e: Exception) {
                // Handle error
                Log.e("PostViewModel", "Error fetching posts with user names", e)
                _bandPosts.value = emptyList()
            }
        }
    }

    // fetch
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

    // Adds a comment to a post.
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

    // Uploads a post with specified content.
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

    // Uploads a post on behalf of the band the user is a member of
    fun uploadBandPost(
        bandId: String,
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

        val bandRef = db.collection("bands").document(bandId)


        // Create a map to represent the post, using the user document reference for the userId
        val postMap = hashMapOf(
            "userId" to userRef, // Use this instead of "userId" to store a reference
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp(),
            "bandId" to bandRef
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

    // uploads feedback that users can give to bands
    fun uploadFeedback(
        bandId: String,
        feedback: String,
        context: Context, // Added context parameter for showing toast messages
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val feedbackCollection = db.collection("feedback")

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "User must be logged in to submit feedback.", Toast.LENGTH_SHORT).show()
            }
            onFailure(Exception("User not logged in"))
            return
        }

        // Create a reference to the user's document in the users collection
        val userRef = db.collection("users").document(currentUserId)

        // Create a reference to the band's document in the bands collection
        val bandRef = db.collection("bands").document(bandId)

        // Create a map to represent the feedback
        val feedbackMap = hashMapOf(
            "userId" to userRef,
            "bandId" to bandRef,
            "timestamp" to FieldValue.serverTimestamp(),
            "feedback" to feedback
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                feedbackCollection.add(feedbackMap).await()
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

    // fetch all feed back for a specific band based on its band ID
    fun fetchFeedbackForBand(bandIdParam: String? = null) {
        viewModelScope.launch {
            try {
                val bandIdToUse = bandIdParam ?: return@launch
                val bandRef = db.collection("bands").document(bandIdToUse)

                // Query the feedbacks collection for documents where the bandId field matches the bandRef
                val result = db.collection("feedback")
                    .whereEqualTo("bandId", bandRef) // Use bandRef directly
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                if (result.isEmpty) {
                    Log.d("FeedbackViewModel", "No feedback found for band")
                } else {
                    Log.d("FeedbackViewModel", "Fetched ${result.size()} feedbacks for band")
                }

                // Initialize an empty list to hold the feedbacks with user names
                val feedbacksWithUserNames = mutableListOf<Map<String, Any>>()

                // Iterate through each document in the result
                for (document in result.documents) {
                    val feedbackData = document.data as MutableMap<String, Any>
                    feedbackData["feedbackId"] = document.id
                    // Get the user reference from the feedback
                    val userRef = feedbackData["userId"] as DocumentReference

                    // Fetch the user document based on the reference
                    val userDoc = userRef.get().await()

                    // Retrieve the user's name from the user document
                    val userName = userDoc.getString("name") ?: "Unknown User"

                    // Add the user's name to the feedback data
                    feedbackData["userName"] = userName

                    // Optionally, add more information or transform data here

                    // Add the modified feedback data to the list
                    feedbacksWithUserNames.add(feedbackData)
                }

                // Update the MutableStateFlow with the modified list of feedbacks
                _bandFeedbacks.value = feedbacksWithUserNames
            } catch (e: Exception) {
                // Handle error
                Log.e("FeedbackViewModel", "Error fetching feedback for band", e)
                _bandFeedbacks.value = emptyList()
            }
        }
    }




}



