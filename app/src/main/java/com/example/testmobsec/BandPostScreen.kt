package com.example.testmobsec


import android.widget.Toast
import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.testmobsec.viewModel.BandViewModel
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BandPostScreen(
    navController: NavController = rememberNavController(),
bandId: String
    ) {
    val profileViewModel: ProfileViewModel = viewModel() // Instantiate the ProfileViewModel to fetch the profile image URL.

    var bandIdState by remember { mutableStateOf<String?>(null) } // State to store the bandId once we retrieve it from Firestore

    val user = FirebaseAuth.getInstance().currentUser // Get the current user.
    val postViewModel: PostViewModel = viewModel() // Instantiate the PostViewModel to upload the post.
    val bandViewModel: BandViewModel = viewModel() // Instantiate the BandViewModel to fetch the band's profile image URL.
    val context = LocalContext.current  // Get the context.
    val focusRequester = remember { FocusRequester() } // Create a FocusRequester to request focus to the text field.
    val keyboardController = LocalSoftwareKeyboardController.current // Get the keyboard controller.
    var postText by remember { mutableStateOf("") } // State to store the post text.
    val profileImageUrl by profileViewModel.profileImageUrl.collectAsState() // Observe the profile image URL from the ProfileViewModel.

    val bandProfileImageUrl by bandViewModel.bandProfileImageUrl.collectAsState() // Observe the band's profile image URL from the BandViewModel.

    // Create an ImagePainter for the band's profile image.
    val bandImagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(data = bandProfileImageUrl)
            .error(R.drawable.ic_launcher_foreground) // Your placeholder image
            .build()
    )


    // Trigger image fetching on composable load or bandId change
    LaunchedEffect(true) {

            bandViewModel.fetchBandProfileImageUrl(bandId)

    }

    // The Scaffold composable provides a consistent layout structure with a top app bar.
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
    ) { paddingValues ->
        // Request focus to the text field and show the keyboard on composable load.
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
            profileViewModel.fetchProfileImageUrl()
        }

        Column(modifier = Modifier.padding(paddingValues)) {
            Row(modifier = Modifier.weight(1f)) {
                profileImageUrl?.let { url ->
                    // When image is clicked, show dialog to confirm image change
                    Image(
                        painter = bandImagePainter,
                        contentDescription = "Band Image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }  ?: Text("No profile image available")
                // BasicTextField for the post text.
                BasicTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    modifier = Modifier
                        .padding(paddingValues) // Apply padding from Scaffold
                        .fillMaxSize() // Fill the available space considering padding
                        .focusRequester(focusRequester), // Request focus
                    textStyle = TextStyle(
                        fontSize = 24.sp, // Set the font size bigger
                    ),

                )

            }
            Spacer(modifier = Modifier.height(8.dp))
            // Button to upload the post.
            Button(
                onClick = {
                    // Upload the post using the PostViewModel.
                    postViewModel.uploadBandPost(
                        bandId = bandId,
                        content = postText,
                        context = context,
                        onSuccess = {
                            // Show a toast message on success.
                            Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show()
                            // Navigate to the band screen.
                            navController.navigate("band_screen/$bandId")
                        },
                        onFailure = { exception ->
                            // Show a toast message on failure.
                            Toast.makeText(context, "Failed to post: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Post")
            }
        }
    }
}




