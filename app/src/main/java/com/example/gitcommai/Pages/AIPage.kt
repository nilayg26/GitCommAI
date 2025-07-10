package com.example.gitcommai.Pages

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.gitcommai.GitCommAIAlertDialogue
import com.example.gitcommai.GitCommAIBottomBar
import com.example.gitcommai.GitCommAITopAppBar
import com.example.gitcommai.R
import com.example.gitcommai.ViewModels.AIState
import com.example.gitcommai.ViewModels.AIViewModel
import com.example.gitcommai.ViewModels.TextRecognizer
import com.example.gitcommai.ViewModels.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class Message(
    val text: String,
    val isBot: Boolean,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeByte(if (isBot) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)
        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun AIPage(
    navController: NavHostController,
    aiViewModel: AIViewModel,
    textRecognizer: TextRecognizer
) {
    var alertDialogInfo = remember {
        AlertDialogInfo(onConfirmationRequest = {})
    }
    var enableAlertDialog by remember {
        mutableStateOf(false)
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context= LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var messages by rememberSaveable { mutableStateOf(emptyList<Message>()) }
    var greetDone by rememberSaveable { mutableStateOf(false) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            textRecognizer.getText(it){text->inputText=text}
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { textRecognizer.getText(it, context) { text -> inputText = text } }
    }
    LaunchedEffect(Unit) {
        GlobalScope.launch {
            if (!greetDone) {
                isTyping = true
                if (aiViewModel.currentState.value!=AIState.Loading && messages.isEmpty()) {
                    val greetResponse = aiViewModel.greetAI()
                    messages = messages + Message(text = greetResponse, isBot = true)
                }
                isTyping = false
                greetDone = true
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    Scaffold(Modifier.fillMaxSize(), topBar = { GitCommAITopAppBar("GitCommAI") }, bottomBar = { GitCommAIBottomBar(navController=navController, selectedIndex = 2) }) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize().padding(paddingValues)
        ) {
            if (enableAlertDialog){
                GitCommAIAlertDialogue(
                    imageVector = alertDialogInfo.imageVector,
                    body = alertDialogInfo.body,
                    onDismissRequest = { enableAlertDialog = false }
                ) {
                   alertDialogInfo.onConfirmationRequest()
                }
            }
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
                    items(messages) { message ->
                        MessageBubble(message = message, aiViewModel,context=context)
                    }
                    if (isTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End){
                    FloatingActionButton(
                        onClick = {
                            alertDialogInfo= AlertDialogInfo(imageVector = Icons.Filled.Info, body = context.getString(R.string.aiPageAlertDialogue), onConfirmationRequest = { galleryLauncher.launch("image/*")}, confirmText = "Continue", dismissText = "Not Now")
                            enableAlertDialog=true
                        },
                        modifier = Modifier.padding(end = 16.dp).size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        GlideImage(
                            model = context.getString(R.string.galleryIconUrl),
                            contentDescription = "Gallery",
                            modifier = Modifier.size(30.dp),
                        )
                    }
                    FloatingActionButton(
                        onClick = {
                            alertDialogInfo= AlertDialogInfo(imageVector = Icons.Filled.Info, body = context.getString(R.string.aiPageAlertDialogue), onConfirmationRequest = {  cameraLauncher.launch(null)}, confirmText = "Continue", dismissText = "Not Now")
                            enableAlertDialog=true
                         //   cameraLauncher.launch(null)
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        GlideImage(
                            model = context.getString(R.string.cameraIconUrl),
                            contentDescription = "Camera",
                            modifier = Modifier.size(30.dp),
                        )
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
                        verticalAlignment = Alignment.CenterVertically,
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
                                        val userMessage = Message(
                                            text = inputText.trim(),
                                            isBot = false
                                        )
                                        messages = messages + userMessage
                                        val userInput = inputText.trim()
                                        inputText = ""
                                        coroutineScope.launch {
                                            isTyping = true
                                            val botResponse = Message(
                                                text = aiViewModel.getAIResponse(str = userInput),
                                                isBot = true
                                            )
                                            isTyping = false
                                            messages = messages + botResponse
                                        }
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
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MessageBubble(message: Message, aiViewModel: AIViewModel,context: Context) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isBot) Arrangement.Start else Arrangement.End
    ) {
        if (message.isBot) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    GlideImage(
                        model = context.getString(R.string.appLogoUrl),
                        contentDescription = "AppLogo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (message.isBot) 4.dp else 20.dp,
                    topEnd = if (message.isBot) 20.dp else 4.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                shadowElevation = if (message.isBot) 2.dp else 0.dp,
                color = if (message.isBot)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.primary
            ) {
                SelectionContainer {
                    Text(
                        text = message.text.replace("```","💻"),
                        modifier = Modifier.padding(12.dp).selectableGroup(),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = if (message.isBot)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (!message.isBot) {
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
                        model = aiViewModel.getAvatarUrl(),
                        contentDescription = "avatar",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                GlideImage(
                    model = "https://nilayg26.github.io/Animation/gitcommailogocompressed_11zon.jpg",
                    contentDescription = "AppLogo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "typing")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                            )
                    )
                }
            }
        }
    }
}