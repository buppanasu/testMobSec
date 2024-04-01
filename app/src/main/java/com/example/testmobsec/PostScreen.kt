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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel

// The PostScreen composable function defines the UI for creating a new post.
@Composable
fun PostScreen(
    navController: NavController = rememberNavController(),

    ) {
    // The ProfileViewModel and PostViewModel are used to manage the data and logic related to the profile image and the post creation process, respectively.
    val profileViewModel: ProfileViewModel = viewModel()
    val postViewModel: PostViewModel = viewModel()
    val context = LocalContext.current // The context is used to display toast messages.
    val focusRequester = remember { FocusRequester() } // The focusRequester is used to request focus on the text field.
    val keyboardController = LocalSoftwareKeyboardController.current // The keyboardController is used to show the software keyboard.
    var postText by remember { mutableStateOf("") }  // The postText state variable is used to store the content of the post entered by the user.
    val profileImageUrl by profileViewModel.profileImageUrl.collectAsState() // The profileImageUrl state variable is used to store the URL of the profile image.

    // The Scaffold composable provides a consistent layout structure with a top app bar and padding.
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
    ) { paddingValues ->
        // The LaunchedEffect block is used to request focus on the text field and show the software keyboard when the screen is first displayed. It also fetches the profile image URL from the ProfileViewModel.
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
            profileViewModel.fetchProfileImageUrl()
        }

        Column(modifier = Modifier.padding(paddingValues)) {
            // The Row composable places the profile image and the text field for the post content side by side.
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

                // The BasicTextField composable allows the user to enter the content of the post.
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
            Button(
                onClick = {
                    // The uploadPost function from the PostViewModel is called to upload the post to the server.
                    postViewModel.uploadPost(
                        content = postText,
                        context = context,
                        onSuccess = {
                            // If the post is uploaded successfully, a toast message is displayed and the user is navigated to the profile screen.
                            Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show()
                            navController.navigate("profile_screen")
                        },
                        onFailure = { exception ->
                            // If the post fails to upload, an error message is displayed.
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



@Preview(showBackground = true)
@Composable
fun PreviewPostScreen() {
    PostScreen()
}
