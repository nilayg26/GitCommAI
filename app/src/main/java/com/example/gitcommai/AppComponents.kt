package com.example.gitcommai

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun AnimationLottie(size: Int = 200, jsonStr:String = ""){
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(
            jsonStr
        )
    )
    val preloaderProgress by animateLottieCompositionAsState(
        preloaderLottieComposition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition =preloaderLottieComposition,
        progress = preloaderProgress,
        modifier = Modifier.size(size.dp)
    )
}
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GitCommAILButton(
    enable: Boolean=true,
    size: Int = 200,
    text: String,
    outline: Boolean = false,
    logo: Boolean=false ,
    onClick: () -> Unit = {}
){
    val context= LocalContext.current
    if (!outline) {
        Button(enabled = enable,onClick = onClick, modifier = Modifier.width(size.dp)) {
            Text(text = text, fontWeight = FontWeight.Bold)
            if (logo){
                GlideImage(modifier = Modifier
                    .padding(start = 10.dp)
                    .size(20.dp)
                    .clip(CircleShape), model =context.getString(R.string.githubLogoUrl), contentDescription = "")
            }
        }
    }
    else{
        OutlinedButton(onClick = onClick, modifier = Modifier.width(size.dp)) {
            Text(text = text)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitCommAITopAppBar(text: String, backButton: Boolean = false,onBackButton:()->Unit={}){
    Row(Modifier.padding(start = 16.dp)) {
        TopAppBar(modifier = Modifier.animateContentSize(),scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = {false}),
            title = {
                Text(text, fontWeight = FontWeight.Bold, fontSize = 32.sp, modifier = Modifier.statusBarsPadding())
            },
            navigationIcon = {
                if (backButton) {
                    IconButton(
                        onClick = onBackButton,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        )
    }
}
//@Composable
//fun GitCommAITopAppBar(text:String){
//   Row(Modifier.padding(start = 16.dp).animateContentSize()) {Text(text, fontWeight = FontWeight.Bold, fontSize = 32.sp, modifier = Modifier.statusBarsPadding()) }
//}
@Composable
fun GitCommAIAlertDialogue( imageVector: ImageVector=Icons.Filled.Warning, body:String="",dismissText:String="Not Now",confirmText:String="Confirm",onDismissRequest: (Boolean) -> Unit, onConfirm: () -> Unit){
    AlertDialog(onDismissRequest = { onDismissRequest(false) },
        confirmButton = {
            Button(onClick = {
                onConfirm();onDismissRequest(false)
            }) { Text(confirmText) }
        },
        dismissButton = {
            Button(
                onClick = { onDismissRequest(false) }
            )
            {
                Text(dismissText)
            }
        },
        title = {
            Row(horizontalArrangement = Arrangement.Center) {
                Text("")
                Icon(
                    imageVector = imageVector,
                    contentDescription = ""
                )
            }
        },
        text = { Text(body) }
    )
}
object BottomBarProps{
    val list= mutableListOf("News","Chat","AI","Acc")
    val mapFilled= mutableMapOf("News" to Icons.Filled.Info,"Chat" to Icons.Filled.Face,"AI" to Icons.Filled.Search,"Acc" to Icons.Filled.AccountCircle)
    val mapOutlined= mutableMapOf("News" to Icons.Outlined.Info,"Chat" to Icons.Outlined.Face,"AI" to Icons.Outlined.Search,"Acc" to Icons.Outlined.AccountCircle)
}
@Composable
fun GitCommAIBottomBar(navController: NavHostController, selectedIndex:Int) {
    NavigationBar {
        BottomBarProps.list.forEachIndexed { idx, it ->
            NavigationBarItem(onClick = {
                navController.navigate(BottomBarProps.list[idx]) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    restoreState = true
                    launchSingleTop = true
                }
            }, selected = selectedIndex == idx, label = { }, icon = {
                if (selectedIndex == idx) {
                    BottomBarProps.mapFilled[it]?.let { it1 ->
                        Icon(
                            contentDescription = it,
                            imageVector = it1
                        )
                    }
                } else {
                    BottomBarProps.mapOutlined[it]?.let { it1 ->
                        Icon(
                            contentDescription = it,
                            imageVector = it1
                        )
                    }
                }
            }
            )
        }
    }
}
@Composable
fun GitCommAIOutlinedTextField(searchQuery:String,onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = {onValueChange(it)},
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        placeholder = { Text("Search GitCommAI Users...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon"
            )
        },
        shape = RoundedCornerShape(50),
        singleLine = true
    )
}