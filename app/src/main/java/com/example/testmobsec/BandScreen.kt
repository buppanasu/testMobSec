
package com.example.testmobsec
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.testmobsec.util.Band
import com.example.testmobsec.viewModel.BandViewModel
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandScreen(navController: NavController = rememberNavController(),bandId: String, bandViewModel: BandViewModel = viewModel()) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val postsViewModel: PostViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val bandDetails = remember { mutableStateOf<Band?>(null) }
    val memberNames = remember { mutableStateListOf<String>() }
    val bandPosts by postsViewModel.bandPosts.collectAsState(initial = emptyList())
    val bandFeedback by postsViewModel.bandFeedbacks.collectAsState(initial = emptyList())
    var bandIdState by remember { mutableStateOf<String?>(null) }
    val followersCount by bandViewModel.followersCount.collectAsState()
    val bandPostsCount by postsViewModel.bandPostsCount.collectAsState()
    var currentTab by remember { mutableStateOf("Feed") }
    val bandProfileImageUrl by bandViewModel.bandProfileImageUrl.collectAsState()
    var isBandCreator = remember {mutableStateOf(false)}
    var showJoinRequestsOverlay by remember { mutableStateOf(false) }
    val joinRequestIds = remember { mutableStateListOf<String>() }
    val firestore = FirebaseFirestore.getInstance()
    fun getUserDocument(userId: String) = firestore.collection("users").document(userId)
    fun getBandDocument(bandId: String) = firestore.collection("bands").document(bandId)
    val joinRequestUserNames = remember { mutableStateListOf<String>() }
    val bandImagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data = bandProfileImageUrl)
            .error(R.drawable.ic_launcher_foreground)
            .build()
    )

    LaunchedEffect(user) {
        user?.let { firebaseUser ->
            FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
                .get().addOnSuccessListener { documentSnapshot ->
                    val bandId = documentSnapshot.getString("bandId")
                    bandIdState = documentSnapshot.getString("bandId")
                    // Once we have the bandId, fetch the band details
                    bandId?.let {
                        FirebaseFirestore.getInstance().collection("bands").document(it)

                            .get().addOnSuccessListener { bandSnapshot ->
                                val band = bandSnapshot.toObject(Band::class.java)
                                bandDetails.value = band
                                // Now fetch the names of each member
                                band?.members?.forEach { memberId ->
                                    val userId = memberId.substringAfterLast("/")
                                    FirebaseFirestore.getInstance().collection("users").document(userId)
                                        .get().addOnSuccessListener {
                                                userSnapshot ->
                                            val name = userSnapshot.getString("name")
                                            if (name != null) {

                                                memberNames.add(name)
                                            }
                                        }
                                }
                            }.addOnFailureListener { e ->
                                Log.e("BandScreen", "Error fetching band details", e)
                            }
                    }
                }
        }

    }

    LaunchedEffect(showJoinRequestsOverlay) {
        Log.d("BandScreen", "Fetching join requests: $showJoinRequestsOverlay")
        if (showJoinRequestsOverlay) {
            joinRequestUserNames.clear()
            firestore.collection("bands").document(bandId)
                .get().addOnSuccessListener { document ->
                    val joinRequestIdsList =
                        document["joinRequests"] as? List<String> ?: emptyList()
                    Log.d("BandScreen", "Join request IDs: $joinRequestIdsList")
                    joinRequestIdsList.forEach { userId ->
                        firestore.collection("users").document(userId)
                            .get().addOnSuccessListener { userDoc ->
                                val userName = userDoc["name"] as? String ?: "Unknown"
                                Log.d("BandScreen", "Join request from user: $userName")
                                joinRequestUserNames.add(userName)
                                joinRequestIds.add(userId) // Make sure to add the userId to the list
                            }
                    }

                }
        }

    }

    // Trigger image fetching on composable load or bandId change
    LaunchedEffect(bandIdState) {
        bandIdState?.let { nonNullBandId ->
            bandViewModel.fetchBandProfileImageUrl(nonNullBandId)
        }
    }

    LaunchedEffect(true) {
        bandViewModel.fetchBandFollowerCount(bandId)
        postsViewModel.fetchPostsCountForBand(bandId)
    }

    // Fetch if the user is the band creator on composable load or when user changes
    LaunchedEffect(key1 = user) {
        user?.let { firebaseUser ->
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
            userDocRef.get().addOnSuccessListener { userSnapshot ->
                // Assuming 'isBandCreator' is a Boolean field in your user document
                isBandCreator.value = userSnapshot.getBoolean("isBandCreator") == true
            }.addOnFailureListener { e ->
                Log.e("BandScreen", "Error fetching user details", e)
            }
        }
    }

    // Dialog state to confirm image change
    var showImageChangeDialog by remember { mutableStateOf(false) }

    // Handle image picking result
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            bandIdState?.let { nonNullBandId ->
                bandViewModel.updateBandProfilePicture(nonNullBandId, selectedImageUri,
                    onSuccess = { /* Handle success, e.g., show a Toast */ },
                    onFailure = { exception -> /* Handle failure, e.g., show a Toast or dialog */ }
                )
            }
        }
    }





    Scaffold( topBar = { TopAppBarContent(navController) },
        bottomBar = { BottomAppBarContent(navController) },
        floatingActionButton = { // Use the floatingActionButton parameter to add the IconButton at the bottom right
            FloatingActionButton(onClick = { navController.navigate("bandPost_screen/$bandId")}) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.End) {paddingValues ->
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(paddingValues)) {



                // ... Your existing UI code
                // Top bar with settings icon, not functional in this example
//                TopAppBar(title = { Text("Band Details") }, actions = {
//                    IconButton(onClick = { /* Handle settings click */ }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                            contentDescription = "Settings"
//                        )
//                    }
//                })

                Spacer(Modifier.height(50.dp))

                Image(
                    painter = bandImagePainter,
                    contentDescription = "Band Image",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { showImageChangeDialog = true }
                )



                if (showImageChangeDialog) {
                    // Implement AlertDialog to confirm the action
                    AlertDialog(
                        onDismissRequest = { showImageChangeDialog = false },
                        title = { Text("Change profile picture?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    pickImageLauncher.launch("image/*")
                                    showImageChangeDialog = false
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showImageChangeDialog = false }
                            ) {
                                Text("No")
                            }
                        }
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Band name
                bandDetails.value?.let { band ->
                    Text(
                        text = band.bandName,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold, // Make it bold
                            fontSize = 34.sp, // Increase font size for emphasis
                            letterSpacing = 0.15.sp, // Adjust letter spacing if necessary
                            color = MaterialTheme.colorScheme.onSurface // Use a color that stands out on your surface color
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp) // Add some padding
                            .align(Alignment.CenterHorizontally) // Center align on the horizontal axis
                    )
                } ?: run {
                    Text(
                        "Loading band details...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(8.dp))



                // Follower stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        "Posts: $bandPostsCount",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp) // Set the font size to 16.sp
                    )
                    Text(
                        "Followers: $followersCount",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp) // Set the font size to 16.sp
                    )
                }

                if (isBandCreator.value) {
                    Button(
                        onClick = { showJoinRequestsOverlay = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Manage Join Requests")
                    }
                }

                // When you want to show the overlay, e.g. in response to a button click
                if (showJoinRequestsOverlay) {
                    JoinRequestsOverlay(
                        joinRequestUserNames = joinRequestUserNames,
                        joinRequestIds = joinRequestIds,
                        // ... inside the JoinRequestsOverlay composable when the Accept button is clicked
                        onAccept = { userId ->
                            // Make sure to pass the bandName here
                            val pureUserId = userId.substringAfterLast("/")
                            bandDetails.value?.let { band ->
                                acceptJoinRequest(bandId, pureUserId, band.bandName)
                                // Update any additional UI if necessary
                                showJoinRequestsOverlay = false
                            }
                        },
                        onReject = { userId ->
                            val pureUserId = userId.substringAfterLast("/")
                            rejectJoinRequest(bandId, pureUserId)
                            // Add any additional UI update logic if necessary after rejecting the request
                            showJoinRequestsOverlay = false // Close overlay after action
                        },
                        onDismiss = { showJoinRequestsOverlay = false }
                    )
                }


                // Tabs for Posts, Artists, and Feed
                val tabTitles = listOf("Posts", "Artists", "Feed")
                var selectedTab by remember { mutableStateOf(0) }
                TabRow(selectedTabIndex = selectedTab) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }


                // Content based on selected tab
                when (selectedTab) {
                    0 -> {
                        postsViewModel.fetchPostsForBand(bandId)

                        LazyColumn {
                            items(bandPosts) { postMap ->
                                val bandName = postMap["bandName"] as? String ?: "No Content"
                                val content = postMap["content"] as? String ?: "No Content"
                                val timestamp = postMap["timestamp"]
                                val postId = postMap["postId"] as String
                                val isLiked by postsViewModel.isPostLikedByUser(postId)
                                    .collectAsState(initial = false)
                                val likesCountFlow = postsViewModel.getLikesCountFlow(postId)
                                val likesCount by likesCountFlow.collectAsState()
                                val commentsCountFlow = postsViewModel.getCommentsCountFlow(postId)
                                val commentsCount by commentsCountFlow.collectAsState()


                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp), // More padding for a spacious look
                                ) {
                                    Column { // Column for the image and action buttons
                                        if (bandProfileImageUrl != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(bandProfileImageUrl),
                                                contentDescription = "Band Image",
                                                modifier = Modifier
                                                    .size(50.dp) // Larger for visibility
                                                    .clip(CircleShape)
                                                    .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape) // Border for definition
                                            )
                                        } else {
                                            Text(
                                                "No profile image available",
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }

                                        // Action buttons row
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 8.dp) // Space between image and action buttons
                                        ) {
                                            // Comments count and button
                                            val hasCommented by postsViewModel.hasUserCommented(postId).collectAsState(initial = false)
                                            IconButton(onClick = { navController.navigate("comment_screen/$postId") }) {
                                                Icon(
                                                    Icons.Filled.Comment,
                                                    contentDescription = "Comment",
                                                    tint = if (hasCommented) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (commentsCount > 0) {
                                                Text(
                                                    text = commentsCount.toString(),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(24.dp)) // Spacing between comment and like buttons

                                            // Likes count and button
                                            val likeIconColor = if (isLiked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                            IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                                                Icon(
                                                    Icons.Filled.ThumbUp,
                                                    contentDescription = "Like",
                                                    tint = likeIconColor
                                                )
                                            }
                                            if (likesCount > 0) {
                                                Text(
                                                    text = likesCount.toString(),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Column for text content
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp, end = 4.dp) // Start padding for spacing from the image, end padding for spacing from edge
                                    ) {
                                        Text(
                                            text = bandName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = content,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = formatDate(timestamp),
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }

                                Divider()
                            }
                        }
                    }



                    1 -> {

                        // Existing code where you display the member names
                        bandDetails.value?.let { band ->
                            Text("Members:", modifier = Modifier.padding(16.dp))
                            memberNames.forEachIndexed { index, name ->
                                // Extract the corresponding userId for each member name
                                val userId = band.members?.get(index)?.substringAfterLast("/") ?: ""
                                Text(
                                    text = name,
                                    modifier = Modifier
                                        .padding(start = 16.dp, bottom = 4.dp)
                                        .clickable {
                                            // When a name is clicked, navigate to OthersProfileScreen with the userId
                                            navController.navigate("othersProfile_screen/$userId")
                                        }
                                )
                            }
                        }

                    }

                    2 -> {
                        postsViewModel.fetchFeedbackForBand(bandId)
                        val profileImageUrls by profileViewModel.profileImageUrls.collectAsState()

                        LazyColumn {
                            items(bandFeedback) { postMap ->
                                val userName = postMap["userName"] as? String ?: "No Content"
                                val content = postMap["feedback"] as? String ?: "No Content"
                                val timestamp = postMap["timestamp"]
                                val userDocRef = postMap["userId"] as? DocumentReference
                                val userId = userDocRef?.id.toString()
                                LaunchedEffect(userId) {
                                    profileViewModel.fetchProfileImageUrlByUserId(userId)
                                }
                                val imageUrl = profileImageUrls[userId]



                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    // Column for the image and action icons
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .padding(end = 16.dp) // Adjust the end padding to bring texts closer to the image
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(imageUrl),
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .size(60.dp) // Adjust the size if necessary
                                                .clip(CircleShape)
                                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        )

                                        // Add action icons below the image here if necessary
                                    }

                                    // Column for texts
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                    ) {
                                        Text(
                                            text = userName,
                                            fontWeight = FontWeight.Bold,
                                            style = TextStyle(fontSize = 18.sp)
                                        )
                                        Text(text = content, style = TextStyle(fontSize = 14.sp))
                                        Text(
                                            text = formatDate(timestamp),
                                            style = TextStyle(fontSize = 12.sp)
                                        )
                                    }
                                    // Add the action icons next to the texts here if necessary
                                }
                                Divider()
                            }
                        }
                    }

                }

            }
        }
    }
}
@Composable
fun JoinRequestsOverlay(
    joinRequestUserNames: List<String>,
    joinRequestIds: List<String>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Define the TextStyle for the names here
    val nameTextStyle = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Bold,
    )

    if (joinRequestUserNames.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("Join Requests", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                LazyColumn {
                    items(joinRequestUserNames.zip(joinRequestIds)) { (name, userId) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically // Align text to center vertically
                        ) {
                            Text(name, style = nameTextStyle)
                            Row {
                                Button(
                                    onClick = { onAccept(userId) },
                                    // Style your buttons here
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Accept")
                                }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { onReject(userId) },
                                    // Style your buttons here
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                        Divider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("Join Requests", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Text("No join requests", style = nameTextStyle)
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }
}

fun acceptJoinRequest(bandId: String, userId: String, bandName: String) {
    val bandDocRef = getBandDocument(bandId)
    val userDocRef = getUserDocument(userId)

    // Start a batch to perform multiple write operations atomically
    val batch = FirebaseFirestore.getInstance().batch()

    // Add user to the members list of the band
    batch.update(bandDocRef, "members", FieldValue.arrayUnion(userId))

    // Remove user from the joinRequests list of the band
    batch.update(bandDocRef, "joinRequests", FieldValue.arrayRemove(userId))

    // Add bandId and bandName to the user's document
    batch.update(userDocRef, mapOf(
        "bandId" to bandId,
        "bandName" to bandName
    ))

    // Commit the batch operation
    batch.commit().addOnSuccessListener {
        Log.d("BandScreen", "User $userId successfully added to band $bandName with bandId $bandId")
    }.addOnFailureListener { e ->
        Log.e("BandScreen", "Error updating band and user documents", e)
    }
}

fun rejectJoinRequest(bandId: String, userId: String) {
    // Remove user from the joinRequests list
    val bandDocRef = getBandDocument(bandId)
    bandDocRef.update("joinRequests", FieldValue.arrayRemove(userId))
}

fun getBandDocument(bandId: String): DocumentReference {
    return FirebaseFirestore.getInstance().collection("bands").document(bandId)
}

fun getUserDocument(userId: String): DocumentReference {
    return FirebaseFirestore.getInstance().collection("users").document(userId)
}