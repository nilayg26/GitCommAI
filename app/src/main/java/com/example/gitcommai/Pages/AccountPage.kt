package com.example.gitcommai.Pages
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.gitcommai.AnimationLottie
import com.example.gitcommai.GitCommAIAlertDialogue
import com.example.gitcommai.GitCommAILButton
import com.example.gitcommai.LoginPage
import com.example.gitcommai.R
import com.example.gitcommai.RepoClasses.ReposItem
import com.example.gitcommai.ViewModels.AnimationViewModel
import com.example.gitcommai.ViewModels.AuthViewModel
import com.example.gitcommai.ViewModels.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AlertDialogInfo(
    val imageVector: ImageVector=Icons.AutoMirrored.Filled.ExitToApp,
    val body: String="You will be directed to Browser",
    val dismissText: String="Not Now",
    val confirmText: String="Go",
    val onConfirmationRequest: ()->Unit
)
@SuppressLint("MutableCollectionMutableState", "UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AccountPage(navController:NavHostController,authViewModel:AuthViewModel,animationViewModel: AnimationViewModel) {
    val context = LocalContext.current
    var alertDialogInfo= remember {
        AlertDialogInfo( onConfirmationRequest = {})
    }
    val user = rememberSaveable {
        mutableStateOf<User?>(null)
    }
    val repos = rememberSaveable {
        mutableStateOf(listOf<ReposItem>())
    }
    var loadingAnimation by remember {
        mutableStateOf("")
    }
    var isLoading by rememberSaveable{
        mutableStateOf(true)
    }
    var enableAlertDialog by remember{
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        val job =launch {
            if (loadingAnimation.isBlank()) {
                loadingAnimation = animationViewModel.getData(key = "loading")
            }
        }
        job.join()
        delay(250)
        val job1 = launch {
            user.value = authViewModel.getUser()
            if (repos.value.isEmpty()) {
                repos.value = authViewModel.getRepos()
            }
        }
        job1.join()
        isLoading=false
    }
        Surface(modifier = Modifier
            .fillMaxSize()) {
            if (enableAlertDialog){
                GitCommAIAlertDialogue(imageVector = alertDialogInfo.imageVector,body = alertDialogInfo.body,dismissText = alertDialogInfo.dismissText,confirmText = alertDialogInfo.confirmText,onDismissRequest = { enableAlertDialog=it }, onConfirm = alertDialogInfo.onConfirmationRequest)
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!isLoading) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    GlideImage(
                                        model = user.value?.avatar_url ?: "",
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        user.value?.name ?: "",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "@${user.value?.login}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    GitCommAILButton(size = 122, text = "Sign Out", onClick = {
                                        enableAlertDialog=true
                                        alertDialogInfo= AlertDialogInfo( onConfirmationRequest = {navController.navigate(LoginPage.route) {
                                            authViewModel.signOut()
                                            popUpTo(0) {
                                                inclusive = true
                                            }
                                        }}, confirmText = "Sign Out", dismissText = "Not Now", imageVector = Icons.AutoMirrored.Filled.ExitToApp, body = "Are you sure you want to Sign Out?")
                                    })
                                }
                            }
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem("Repos", user.value?.public_repos ?: 0)
                                StatItem("Followers", user.value?.followers ?: 0)
                                StatItem("Following", user.value?.following ?: 0)
                            }
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        enableAlertDialog=true
                                        alertDialogInfo= AlertDialogInfo( onConfirmationRequest = {
                                            val intent =
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(user.value?.html_url)
                                                )
                                            context.startActivity(intent)
                                        })
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "🌐 View GitHub Profile",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "📦 Repositories (${repos.value.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(repos.value) { repo ->
                        RepoItemCard(repo) {bool, url ->enableAlertDialog =bool  ;alertDialogInfo= AlertDialogInfo( onConfirmationRequest = {
                            val intent =
                                if (url.isBlank()) {
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(repo.html_url)
                                    )
                                }
                            else{
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(url)
                                )
                            }
                            context.startActivity(intent)
                        }) }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    items(1) {
                        if (loadingAnimation.isNotBlank()) {
                            AnimationLottie(jsonStr = loadingAnimation)
                        }
                    }
                }
            }

        }
    }

@Composable
fun StatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
@Composable
fun RepoItemCard(repo: ReposItem, enableAlertDialog :(Boolean, String)->Unit) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                enableAlertDialog(true,"")
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (repo.name.length>20) repo.name.substring(0,20)+"..." else repo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (repo.private) "Private" else "Public",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (repo.private) Color.Red else Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            repo.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeatureChip("Issues", repo.has_issues)
                FeatureChip("Fork", repo.fork)
                FeatureChip("Pages", repo.has_pages)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "📅 Created: ${repo.created_at.substringBefore("T")}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "🕘 Last Updated: ${repo.updated_at.substringBefore("T")}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "🔗 ${repo.full_name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))
            ClickableUrlLabel("🚀 Releases", context.getString(R.string.githubUrl)+repo.full_name+"/releases",enableAlertDialog)
        }
    }
}
@Composable
fun ClickableUrlLabel(label: String, url: String, enableAlertDialog: (Boolean, String) -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable {
                enableAlertDialog(true,url)
            }
            .padding(vertical = 2.dp)
    )
}
@Composable
fun FeatureChip(label: String, isActive: Boolean) {
    val bgColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f)
    val textColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
