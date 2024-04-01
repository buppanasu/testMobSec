package com.example.testmobsec

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.testmobsec.util.Chat
import com.example.testmobsec.viewModel.ChatViewModel
import com.example.testmobsec.viewModel.ChatViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// The ChatScreen composable function defines the UI for a chat screen between two users.
@Composable
fun ChatScreen(
    navController: NavController = rememberNavController(),
    id: String
) {


    // Instantiate the ViewModel with senderId (current user's ID) and receiverId (band's ID)
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(id))

    // Observe the chats, user name, current user name, and user band ID from the ChatViewModel.
    val chats by chatViewModel.chats.collectAsState()
    val userName by chatViewModel.userName.collectAsState()
    val currentName by chatViewModel.currentName.collectAsState()
    val userBandId by chatViewModel.userBandId.collectAsState()

    // Fetch the band's name, current user's name, and user's role and band ID when the screen is first displayed.
    LaunchedEffect(true){
        chatViewModel.fetchNameById(id)
        chatViewModel.fetchCurrentUserName()
        chatViewModel.fetchUserRoleAndBand()
    }


    // The Scaffold composable provides a consistent layout structure with a top app bar and bottom bar.
    Scaffold(
        topBar = { TopAppBarContent(navController = navController) },
        bottomBar = { BottomAppBarContent(navController) }
    ) {
            paddingValues->
            Column {

                // Display the chat messages in a LazyColumn.
                userName?.let {
                    currentName?.let { it1 ->
                        ChatMessagesUI(chats,id, it, it1,
                            chatViewModel,userBandId, Modifier.padding(paddingValues))
                    }
                }
            }

                    }
}
// The ChatMessagesUI composable function defines the UI for displaying the chat messages.
@Composable
fun ChatMessagesUI(chats: List<Chat>,id: String,userName:String,currentName:String, chatViewModel: ChatViewModel,userBandId: String?, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
//    val scope = rememberCoroutineScope()

    // Scroll to the bottom of the chat when a new message is added.
    LaunchedEffect(chats.size) {
        if (chats.isNotEmpty()) {
            listState.animateScrollToItem(chats.size - 1)
        }
    }

    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // The Box composable is used to position the chat messages and input field.
    Box(
        modifier = modifier.fillMaxSize()) {
        Column {
            // Display chat messages in a LazyColumn that fills the available space but leaves room for the input field
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 70.dp) // Reserve space for the input field and send button
                ,state= listState
            ) {
                items(chats) { chat ->
//                    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: return@items
                    // Chat message UI
                    Box(
                        contentAlignment = when {
                            chat.senderId == currentUserID -> Alignment.CenterEnd
                            chat.senderId == userBandId -> Alignment.CenterEnd // Treat band messages the same as user messages
                            else -> Alignment.CenterStart
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Column(
                            horizontalAlignment = if (chat.senderId == currentUserID) Alignment.End else Alignment.Start,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            // Display the sender's name.
                            Text(
                                text = chat.senderName,
                                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            // Display the chat message.
                            Text(
                                text = chat.message,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .background(
                                        color = if (chat.senderId == currentUserID) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

        }

        // Ensure there's space for the input field and it's not covered by the LazyColumn
        var newMessageText by remember { mutableStateOf("") }
        OutlinedTextField(
            value = newMessageText,
            onValueChange = { newMessageText = it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Type a message...") }
        )

        Button(
            onClick = {
                if (newMessageText.isNotEmpty()) {
                    // Create a new chat object with the current user's ID, name, receiver's name, receiver's ID, message, and timestamp.
                    val newChat = Chat(
                        senderId = currentUserID,
                        senderName = currentName,
                        receiverName = userName,
                        receiverId = id,
                        message = newMessageText,
                        timestamp = System.currentTimeMillis()
                    )
                    chatViewModel.sendChat(newChat) // Send the new chat message using the ChatViewModel.
                    newMessageText = "" // Clear input field after sending
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Text("Send")
        }
    }


}




