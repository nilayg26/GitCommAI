package com.example.gitcommai.Pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.gitcommai.ViewModels.ChatMessage
import com.example.gitcommai.ViewModels.ChatViewModel
import com.example.gitcommai.ViewModels.User
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay

@Composable
fun ChatMessagePage(chatViewModel: ChatViewModel,onTitleChange: (String) -> Unit) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var adminUser by rememberSaveable { mutableStateOf(User()) }
    var otherUser by rememberSaveable { mutableStateOf(User()) }
    val currentUserId = remember { chatViewModel.getUserId() }
    val currentChatRoomId = remember { chatViewModel.getCurrentChatRoomId() }
    val messagesList by remember { derivedStateOf { chatViewModel.messagesList } }

    LaunchedEffect(Unit) {
        println("Only called once")
        chatViewModel.getAllMessageSnapShot(currentChatRoomId)
        val adminId = chatViewModel.getUserId()
        val otherUserId = chatViewModel.getOtherUserId(currentChatRoomId)
        adminUser = chatViewModel.getUser(adminId) ?: User()
        otherUser = chatViewModel.getUser(otherUserId) ?: User()
    }
    LaunchedEffect(messagesList.size) {
        println("ChatList is $messagesList")
        if (messagesList.isNotEmpty()) {
            delay(500)
            listState.animateScrollToItem(messagesList.size - 1)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = messagesList,
                    key = { message -> message.time?.toDate()?.time ?: System.currentTimeMillis() }
                ) { message ->
                    ChatMessageItem(
                        message = message,
                        currentUserId = currentUserId,
                        adminUser = adminUser,
                        otherUser = otherUser,
                        chatViewModel = chatViewModel
                    )
                }
            }
            MessageInputSection(
                inputText = inputText,
                onInputChange = { inputText = it },
                isTyping = isTyping,
                onSendMessage = {
                    if (inputText.isNotBlank()) {
                        chatViewModel.sendMessage(
                            chatRoomId = currentChatRoomId,
                            message = ChatMessage(
                                text = chatViewModel.encodeMessage(inputText),
                                sender = currentUserId,
                                time = Timestamp.now()
                            )
                        )
                        inputText = ""
                    }
                }
            )
        }
    }
}

@Composable
private fun MessageInputSection(
    inputText: String,
    onInputChange: (String) -> Unit,
    isTyping: Boolean,
    onSendMessage: () -> Unit
) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                placeholder = { Text("Type to Chat...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            AnimatedVisibility(!isTyping) {
                FloatingActionButton(
                    onClick = onSendMessage,
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
@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    currentUserId: String,
    adminUser: User,
    otherUser: User,
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel
) {
    // Memoize computed values to avoid recalculation
    val isCurrentUser = remember(message.sender, currentUserId) {
        message.sender == currentUserId
    }
    val formattedTime = remember(message.time) {
        formatTimestamp(message.time)
    }
    val avatarUrl = remember(isCurrentUser, adminUser.avatar_url, otherUser.avatar_url) {
        if (isCurrentUser) adminUser.avatar_url else otherUser.avatar_url
    }

    Row(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            if (avatarUrl != null) {
                AvatarImage(
                    imageUrl = avatarUrl,
                    size = 32.dp,
                    contentDescription = "Other user avatar"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.widthIn(max = 280.dp)) {
            MessageBubble(
                message = message,
                isCurrentUser = isCurrentUser,
                chatViewModel = chatViewModel
            )

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // Right avatar (for current user's messages)
        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            if (avatarUrl != null) {
                AvatarImage(
                    imageUrl = avatarUrl,
                    size = 32.dp,
                    contentDescription = "Current user avatar"
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean,
    chatViewModel: ChatViewModel
) {
    Surface(
        shape = RoundedCornerShape(
            topStart = if (isCurrentUser) 20.dp else 4.dp,
            topEnd = if (isCurrentUser) 4.dp else 20.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        ),
        shadowElevation = if (!isCurrentUser) 2.dp else 0.dp,
        color = if (isCurrentUser)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        SelectionContainer {
            Text(
                text = chatViewModel.decodeMessage(message.text),
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = if (isCurrentUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun AvatarImage(
    imageUrl: String,
    size: Dp,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
                key(imageUrl) {
                    GlideImage(
                        model = imageUrl.ifBlank { null },
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize()
                    )
                }

        }
    }
}

