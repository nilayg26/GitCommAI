package com.example.gitcommai.Pages
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.gitcommai.ChatMessage
import com.example.gitcommai.GitCommAIOutlinedTextField
import com.example.gitcommai.ViewModels.AuthViewModel
import com.example.gitcommai.ViewModels.ChatViewModel
import com.example.gitcommai.ViewModels.LastMessage
import com.example.gitcommai.ViewModels.User
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
@Composable
fun ChatPage(navController: NavHostController, authViewModel: AuthViewModel, chatViewModel: ChatViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf(listOf<User>()) }
    var adminUser by rememberSaveable { mutableStateOf(User()) }
    var userLogin by rememberSaveable { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Debounce search to avoid excessive API calls
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(300) // 300ms debounce
            chatViewModel.searchUser(searchQuery, onSuccess = { searchResult = it })
        } else {
            searchResult = emptyList()
        }
    }
    LaunchedEffect(Unit) {
        adminUser = authViewModel.getUser()
        if (adminUser?.login==null){
            userLogin=""
        }
        else{
            userLogin=adminUser.login
        }
    }
    val chatList by remember { derivedStateOf { chatViewModel.chatList } }
    LaunchedEffect(Unit){
        if (chatList.isEmpty()){
            chatViewModel.getChatRoomsSnapShot(userLogin)
        }
    }
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                GitCommAIOutlinedTextField(searchQuery) { searchQuery = it }
                LazyColumn(modifier = Modifier.animateContentSize()) {
                    when {
                        userLogin.isNotBlank() && searchResult.isEmpty() -> {
                            items(
                                items = chatList.sortedByDescending { it.time },
                            ) { chat ->
                                ChatListItem(
                                    chat = chat,
                                    userLogin = userLogin,
                                    navController = navController,
                                    chatViewModel = chatViewModel
                                )
                                HorizontalDivider()
                            }
                        }

                        searchResult.isNotEmpty() -> {
                            items(
                                items = searchResult,
                                key = { user -> user.login },
                            ) { user ->
                                ChatSearchItem(user) {
                                    coroutineScope.launch {
                                        chatViewModel.addChatRoom(adminUser, it)
                                        searchResult = emptyList()
                                        searchQuery = ""
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ChatSearchItem(otherUser: User, onClick: (User) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onClick(otherUser) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            GlideImage(
                model = otherUser.avatar_url,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "Sender's avatar"
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = otherUser.login,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ChatListItem(
    chat: LastMessage,
    userLogin: String,
    navController: NavHostController,
    chatViewModel: ChatViewModel
) {
    val otherUserIndex = remember(chat.users, userLogin) {
        if (chat.users.getOrNull(0) != userLogin) 0 else 1
    }
    val otherUserName = remember(chat.users, otherUserIndex) {
        chat.users.getOrNull(otherUserIndex) ?: ""
    }
    val otherUserAvatar = remember(chat.avatar_url, otherUserIndex) {
        chat.avatar_url.getOrNull(otherUserIndex) ?: ""
    }
    val lastMessageText = remember(chat.sender, chat.text) {
        "${chat.sender}: ${chat.text?.let { chat.roomId?.let { it1 -> chatViewModel.decodeMessage(it, chatRoomId = it1) } }}"
    }
    val formattedTime = remember(chat.time) {
        formatTimestamp(chat.time)
    }
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable {
                coroutineScope.launch {
                    delay(50)
                    navController.navigate(ChatMessage.route) {
                        chat.roomId?.let { chatViewModel.setCurrentChatRoomId(chatRoomId = it) }
                        launchSingleTop = true
                    }
                    delay(50)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            GlideImage(
                model = otherUserAvatar,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "Sender's avatar"
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = otherUserName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = lastMessageText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

private val timestampFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

fun formatTimestamp(timestamp: Timestamp?): String {
    return timestamp?.let { timestampFormatter.format(it.toDate()) } ?: ""
}