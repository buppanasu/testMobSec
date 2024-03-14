package com.example.testmobsec
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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

    // State to store the bandId once we retrieve it from Firestore
    var bandIdState by remember { mutableStateOf<String?>(null) }


    val followersCount by bandViewModel.followersCount.collectAsState()
    val bandPostsCount by postsViewModel.bandPostsCount.collectAsState()

    var currentTab by remember { mutableStateOf("Feed") }

    // State variables for UI
    val bandProfileImageUrl by bandViewModel.bandProfileImageUrl.collectAsState()
    val bandImagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data = bandProfileImageUrl)
            .error(R.drawable.ic_launcher_foreground) // Your placeholder image
            .build()
    )


    // Fetch the user's bandId and band details from Firestore
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

                // When image is clicked, show dialog to confirm image change
                Image(
                    painter = bandImagePainter,
                    contentDescription = "Band Image",
                    modifier = Modifier
                        .size(120.dp)
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
                        style = MaterialTheme.typography.headlineMedium
                    )
                } ?: run {
                    Text("Loading band details...")
                }

                Spacer(Modifier.height(8.dp))



                // Follower stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Posts: $bandPostsCount")
                    Text("Followers: $followersCount")

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
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(35.dp))
                                    bandProfileImageUrl?.let { url ->
                                        Image(
                                            painter = bandImagePainter,
                                            contentDescription = "Band Image",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                    } ?: Text("No profile image available")
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = bandName, fontWeight = FontWeight.Bold,
                                            style = TextStyle(fontSize = 18.sp)
                                        )
                                        Divider()
                                        Text(text = content, style = TextStyle(fontSize = 14.sp))
                                        Text(
                                            text = formatDate(timestamp),
                                            style = TextStyle(fontSize = 12.sp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                    }
                                    val hasCommented by postsViewModel.hasUserCommented(postId)
                                        .collectAsState(
                                            initial = false
                                        )
                                    val commentedIconColor =
                                        if (hasCommented) Color.Magenta else Color.Gray
                                    IconButton(onClick = { navController.navigate("comment_screen/$postId") }) {
                                        Icon(
                                            Icons.Filled.Comment,
                                            contentDescription = "Comment",
                                            tint = commentedIconColor
                                        )
                                    }
                                    if (commentsCount > 0) {
                                        Text(text = "$commentsCount")
                                    }

                                    // Like button with real-time color change based on like status
                                    val likeIconColor = if (isLiked) Color.Blue else Color.Gray
                                    IconButton(onClick = { postsViewModel.toggleLike(postId) }) {
                                        Icon(
                                            Icons.Filled.ThumbUp,
                                            contentDescription = "Like",
                                            tint = likeIconColor
                                        )
                                    }

                                    // Display likes count, updating in real-time
                                    if (likesCount > 0) {
                                        Text(text = "$likesCount")
                                    }
                                }
                                Divider()
                            }
                        }
                    }



                    1 -> {

                        // TODO: Display artists content
                        bandDetails.value?.let { band ->
                            Text("Members:", modifier = Modifier.padding(16.dp))
                            memberNames.forEach { name ->

                                Text(
                                    name,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
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
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(35.dp))
                                    Image(
                                        painter = rememberAsyncImagePainter(imageUrl),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    ) ?: Text("No profile image available")
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = userName, fontWeight = FontWeight.Bold,
                                            style = TextStyle(fontSize = 18.sp)
                                        )
                                        Divider()
                                        Text(text = content, style = TextStyle(fontSize = 14.sp))
                                        Text(
                                            text = formatDate(timestamp),
                                            style = TextStyle(fontSize = 12.sp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                    }

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