package com.example.gitcommai.Pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.gitcommai.ViewModels.ChatMessage
import com.example.gitcommai.ViewModels.ChatViewModel
import com.example.gitcommai.ViewModels.User
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatMessagePage(chatViewModel: ChatViewModel){
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var adminUser by rememberSaveable {
        mutableStateOf(User())
    }
    var otherUser by rememberSaveable {
        mutableStateOf(User())
    }
    LaunchedEffect(Unit) {
        launch {
            chatViewModel.getAllMessageSnapShot(chatViewModel.getCurrentChatRoomId())
        }
        val adminId = chatViewModel.getUserId()
        val otherUserId = chatViewModel.getOtherUserId(chatViewModel.getCurrentChatRoomId())
        adminUser = chatViewModel.getUser(adminId) ?: User()
        otherUser = chatViewModel.getUser(otherUserId) ?: User()
    }
    LaunchedEffect(chatViewModel.messagesList.size) {
        println("ChatList is"+chatViewModel.messagesList.toString())
        if (chatViewModel.messagesList.isNotEmpty()) {
            listState.animateScrollToItem(chatViewModel.messagesList.size - 1)
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f) // This is crucial - takes remaining space
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatViewModel.messagesList) { message ->
                    ChatMessageItem(message,chatViewModel= chatViewModel, adminUser = adminUser, otherUser = otherUser)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        placeholder = {
                            Text("Type to Chat...")
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    AnimatedVisibility(!isTyping) {
                        FloatingActionButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                   chatViewModel.sendMessage(chatRoomId = chatViewModel.getCurrentChatRoomId(),
                                       message= ChatMessage(text = inputText, sender = chatViewModel.getUserId(), Timestamp.now()))
                                    inputText=""
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = if (inputText.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(20.dp),
                                tint = if (inputText.isNotBlank())
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel,
    adminUser: User,
    otherUser: User
) {
    val dateFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val userid = remember {
        mutableStateOf(chatViewModel.getUserId())
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if ( message.sender==userid.value) Arrangement.End else Arrangement.Start
    ) {
        if (message.sender!=chatViewModel.getUserId()) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Replace with user avatar if needed
                    GlideImage(
                        model = otherUser.avatar_url,
                        contentDescription = "user avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.widthIn(max = 280.dp)) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if ( message.sender==userid.value) 20.dp else 4.dp,
                    topEnd = if ( message.sender==userid.value) 4.dp else 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                shadowElevation = if (message.sender != userid.value) 2.dp else 0.dp,
                color = if (message.sender==userid.value)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                SelectionContainer {
                    Text(
                        text = message.text,
                        modifier = Modifier
                            .padding(12.dp),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = if ( message.sender==userid.value)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = dateFormatter.format(message.time?.toDate() ?: Timestamp.now()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        if (message.sender==chatViewModel.getUserId()) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    GlideImage(
                        model = adminUser.avatar_url,
                        contentDescription = "user avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
