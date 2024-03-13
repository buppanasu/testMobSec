
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.testmobsec.BottomAppBarContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.testmobsec.R
import com.example.testmobsec.TopAppBarContent
import com.example.testmobsec.util.Band

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandScreen(navController: NavController = rememberNavController(), bandViewModel: BandViewModel = viewModel()) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val bandDetails = remember { mutableStateOf<Band?>(null) }
    val memberNames = remember { mutableStateListOf<String>() }

    // State to store the bandId once we retrieve it from Firestore
    var bandIdState by remember { mutableStateOf<String?>(null) }

    // Replace these with actual values or fetch from Firestore
    var followersCount by remember { mutableStateOf(400) }
    var followingCount by remember { mutableStateOf(100) }
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
        bottomBar = { BottomAppBarContent(navController) }) {paddingValues ->
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

                //            // Follow button
                //            Button(onClick = { /* Handle follow click */ }) {
                //                Text("Follow+")
                //            }


                // Follower stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Followers: $followersCount")
                    Text("Following: $followingCount")
                }

                // Tabs for Posts, Artists, and Feed
                val tabTitles = listOf("Posts", "Artists", "Feed")
                var selectedTabIndex by remember { mutableStateOf(0) }
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Content based on selected tab
                when (tabTitles[selectedTabIndex]) {
                    "Posts" -> {
                        // TODO: Display posts content
                        Text("Posts content goes here", modifier = Modifier.padding(16.dp))
                    }


                    "Artists" -> {
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

                    "Feed" -> {
                        // TODO: Replace with actual feed content
                        Text("Feed content goes here", modifier = Modifier.padding(16.dp))
                    }

                }

            }
        }
    }
}