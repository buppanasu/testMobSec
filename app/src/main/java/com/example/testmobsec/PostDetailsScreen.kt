package com.example.testmobsec
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
import coil.compose.rememberAsyncImagePainter
import com.example.testmobsec.R
import com.example.testmobsec.viewModel.PostViewModel
import com.example.testmobsec.viewModel.ProfileViewModel
import java.util.Locale


@Composable
fun PostDetailsScreen(
    navController: NavController = rememberNavController(),
    postId: String
) {


    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        // BottomBar or FloatingActionButton can be added here if needed
    ) { paddingValues ->


        Column(modifier = Modifier.padding(paddingValues)) {
            //TODO: show post and get all comments related to the post
        }
        }
}



//@Preview(showBackground = true)
//@Composable
//fun PreviewCommentScreen() {
//    CommentScreen()
//}
