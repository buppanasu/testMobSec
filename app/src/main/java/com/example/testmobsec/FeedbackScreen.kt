package com.example.testmobsec



import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.example.testmobsec.viewModel.BandViewModel
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FeedbackScreen(
    navController: NavController = rememberNavController(),
    bandId: String
) {
    val profileViewModel: ProfileViewModel = viewModel()
    // State to store the bandId once we retrieve it from Firestore
    var bandIdState by remember { mutableStateOf<String?>(null) }
    val user = FirebaseAuth.getInstance().currentUser
    val postViewModel: PostViewModel = viewModel()
    val bandViewModel: BandViewModel = viewModel()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var postText by remember { mutableStateOf("") }
    val profileImageUrl by profileViewModel.profileImageUrl.collectAsState()




    // Trigger image fetching on composable load or bandId change
    LaunchedEffect(true) {

        profileViewModel.fetchProfileImageUrl()

    }

    // The Scaffold composable provides a consistent layout structure with a top app bar and padding
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
    ) { paddingValues ->
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
            profileViewModel.fetchProfileImageUrl()
        }

        Column(modifier = Modifier.padding(paddingValues)) {
            Row(modifier = Modifier.weight(1f)) {
                profileImageUrl?.let { url ->
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = "",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }  ?: Text("No profile image available")
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
                    // Add more parameters as needed for styling and functionality
                )

            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    postViewModel.uploadFeedback(
                        bandId = bandId,
                        feedback = postText,
                        context = context,
                        onSuccess = {
                            Toast.makeText(context, "Feedback sent successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onFailure = { exception ->
                            Toast.makeText(context, "Failed to post: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Give Feedback")
            }
        }
    }
}




