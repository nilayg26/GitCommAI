package com.example.gitcommai
import NewsPage
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gitcommai.Pages.AIPage
import com.example.gitcommai.Pages.AccountPage
import com.example.gitcommai.Pages.ChatMessagePage
import com.example.gitcommai.Pages.ChatPage
import com.example.gitcommai.Pages.LoginPage
import com.example.gitcommai.ViewModels.AIViewModel
import com.example.gitcommai.ViewModels.AnimationViewModel
import com.example.gitcommai.ViewModels.AuthViewModel
import com.example.gitcommai.ViewModels.ChatViewModel
import com.example.gitcommai.ViewModels.NewsViewModel
import com.example.gitcommai.ui.theme.GitCommAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences= this.getSharedPreferences("GitCommAI", Context.MODE_PRIVATE)
        val authViewModel :AuthViewModel by viewModels<AuthViewModel>(
            factoryProducer = {
                object : ViewModelProvider.Factory{
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return (AuthViewModel(sharedPreferences) as T)
                    }
                }
            }
        )
        val animationViewModel :AnimationViewModel by viewModels<AnimationViewModel>(
            factoryProducer = {
                object : ViewModelProvider.Factory{
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return (AnimationViewModel(sharedPreferences) as T)
                    }
                }
            }
        )
        val aiViewModel :AIViewModel by viewModels<AIViewModel>(
            factoryProducer = {
                object : ViewModelProvider.Factory{
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return (AIViewModel(sharedPreferences) as T)
                    }
                }
            }
        )
        val chatViewModel :ChatViewModel by viewModels<ChatViewModel>(
            factoryProducer = {
                object : ViewModelProvider.Factory{
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return (ChatViewModel(sharedPreferences) as T)
                    }
                }
            }
        )
        val newsViewModel : NewsViewModel by viewModels<NewsViewModel>()
        enableEdgeToEdge()
        setContent {
            GitCommAITheme {
                    Navigation(sharedPreferences,authViewModel,animationViewModel,newsViewModel,aiViewModel,chatViewModel)
            }
        }
    }
}


@Composable
fun Navigation(
    sharedPreferences: SharedPreferences,
    authViewModel: AuthViewModel,
    animationViewModel: AnimationViewModel,
    newsViewModel: NewsViewModel,
    aiViewModel: AIViewModel,
    chatViewModel: ChatViewModel
) {
    var topBarLabel by rememberSaveable {
        mutableStateOf("")
    }
    var bottomBarIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    val token= sharedPreferences.getString("access_token","")?:""
    val userId=sharedPreferences.getString("user_id","")?:""
    val user_name=sharedPreferences.getString("user_name","")?:""
    LaunchedEffect(Unit){
        if (userId.isNotBlank()) {
            chatViewModel.getChatRoomsSnapShot(userId)
        }
    }
    val startPage by remember {
        mutableStateOf(if (token.isNotBlank()){NewsPage.route}else{LoginPage.route})
    }
    val navController = rememberNavController()
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {if(topBarLabel.isNotBlank()){GitCommAITopAppBar(topBarLabel)}}, bottomBar = {if(bottomBarIndex!=-1) {GitCommAIBottomBar(navController = navController,bottomBarIndex)} }) { paddingValues ->
        NavHost(modifier = Modifier.padding(paddingValues).fillMaxSize(),
            navController = navController, startDestination = startPage
        ) {
            composable(LoginPage.route) {
                topBarLabel=""
                bottomBarIndex=-1
                LoginPage(navController, authViewModel, animationViewModel)
            }
            composable(NewsPage.route) {
                topBarLabel= if (userId.isNotBlank()){
                    "Hello ${user_name.split(" ")[0]}!"
                } else{"Daily TechNews" }
                bottomBarIndex=0
                NewsPage(
                    navController,
                    newsViewModel = newsViewModel,
                    animationViewModel = animationViewModel
                )
            }
            composable(ChatPage.route) {
                topBarLabel="GitChat"
                bottomBarIndex=1
                ChatPage(navController, authViewModel, chatViewModel)
            }
            composable(AIPage.route) {
                topBarLabel="GitCommAI"
                bottomBarIndex=2
                AIPage(navController, aiViewModel = aiViewModel)
            }
            composable(AccountPage.route) {
                topBarLabel="Account"
                bottomBarIndex=3
                AccountPage(navController, authViewModel, animationViewModel,chatViewModel)
            }
            composable(ChatMessage.route) {
                topBarLabel="Let's Chat"
                bottomBarIndex=-1
                ChatMessagePage(chatViewModel){
                    topBarLabel=it
                }
            }
        }
    }
}