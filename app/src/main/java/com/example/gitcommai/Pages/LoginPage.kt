package com.example.gitcommai.Pages

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gitcommai.AccountPage
import com.example.gitcommai.AnimationLottie
import com.example.gitcommai.GitCommAILButton
import com.example.gitcommai.NewsPage
import com.example.gitcommai.ViewModels.AnimationViewModel
import com.example.gitcommai.ViewModels.AuthViewModel
import com.example.gitcommai.ViewModels.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    navController: NavController,
    authViewModel: AuthViewModel,
    animationViewModel: AnimationViewModel
) {
    val context= LocalContext.current
    val coroutineScope= rememberCoroutineScope()
    var isLoading by remember {
        mutableStateOf(false)
    }
    val loginAnimation by remember {
        derivedStateOf { animationViewModel.currentAnimation }
    }
    LaunchedEffect(Unit){
        coroutineScope.launch {
           animationViewModel.getData(key="login")
        }
    }
    LaunchedEffect(authViewModel.currentState.value , ChatViewModel.registrationState){
        when(authViewModel.currentState.value){
            "access_token_retrieved" ,"registered" ->{navController.navigate(NewsPage.route){launchSingleTop=true;popUpTo(0){inclusive=true} }}
            "loading"->{isLoading=true}
            "error"->{isLoading=false}
        }
    }
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize().animateContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(20.dp))
                        AnimatedVisibility(loginAnimation.value.isNotBlank()) {
                            AnimationLottie(size = 370, jsonStr = loginAnimation.value)
                        }
                        Text(
                            "GitCommAI\n",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp
                        )
                        Text(
                            "Chat, Code, Connect - Smarter with AI", textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(70.dp))
                        GitCommAILButton(text = "Continue with", logo = true, enable = !isLoading) {
                            coroutineScope.launch {
                                authViewModel.login(context as Activity)
                            }
                        }
                }
            }
        }
