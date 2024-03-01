import android.icu.text.SimpleDateFormat
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testmobsec.R
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PostScreen(
    navController: NavController = rememberNavController(),

) {
    val postViewModel: PostViewModel = viewModel()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var postText by remember { mutableStateOf("") }

    Scaffold(
        topBar = { /* TopAppBarContent(navController = navController) */ },
        // BottomBar or FloatingActionButton can be added here if needed
    ) { paddingValues ->
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }

        Column(modifier = Modifier.padding(paddingValues)) {
            Row(modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
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
                // Your existing Row content
                // Consider placing the TextField and Image in a way that fits your UI design
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    postViewModel.uploadPost(
                        content = postText,
                        context = context,
                        onSuccess = {
                            Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show()
                            navController.navigate("profile_screen")
                        },
                        onFailure = { exception ->
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
