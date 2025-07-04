import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.gitcommai.AnimationLottie
import com.example.gitcommai.GitCommAIAlertDialogue
import com.example.gitcommai.ViewModels.AnimationViewModel
import com.example.gitcommai.ViewModels.NewsStatus
import com.example.gitcommai.ViewModels.NewsViewModel
import kotlinx.coroutines.launch
import org.example.gitcommai.NewsClasses.Article

@Composable
fun NewsPage(navController: NavHostController,newsViewModel: NewsViewModel, animationViewModel: AnimationViewModel){
    var showNews by rememberSaveable{
        mutableStateOf(false)
    }
    var json by remember{
        mutableStateOf("")
    }
    LaunchedEffect(Unit){
        launch {
            json= animationViewModel.getData(key = "loading")
        }
        launch {
            newsViewModel.getData()
        }
    }
    val context= LocalContext.current
    LaunchedEffect(newsViewModel.currentState.value){
        when(newsViewModel.currentState.value) {
            NewsStatus.Initalised -> showNews = true
            NewsStatus.Error-> showNews = true
        }
    }
    var enableAlertDialog by remember {
        mutableStateOf(false)
    }
    var url by remember {
        mutableStateOf("")
    }
   Surface(modifier = Modifier.fillMaxSize()) {
       Column(
           Modifier
               .padding(16.dp)
               .fillMaxSize(),
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.Center
       ) { }
       if (!showNews && json.isNotBlank()) {
           AnimationLottie(jsonStr = json)
       }
       if (enableAlertDialog) {
           GitCommAIAlertDialogue( imageVector = Icons.AutoMirrored.Filled.ExitToApp, body = "Want to read full Article? You will be redirected to Browser", dismissText = "Not Now", confirmText = "Go", onDismissRequest = {enableAlertDialog=it}, onConfirm = {val intent=Intent(Intent.ACTION_VIEW, Uri.parse(url));context.startActivity(intent)})

       }
       if (newsViewModel.mainNews.value!=null) {
           AnimatedVisibility(showNews) {
               ArticleList(
                   articles = newsViewModel.mainNews.value!!.articles,
                   onArticleClick = { enableAlertDialog = true;url = it })
           }
       }
       else if(showNews){
           Text(" Could not fetch News at the moment 🥲, \"Check your internet and restart the app\"")
       }
   }
    }

@Composable
fun ArticleList(articles: List<Article>, onArticleClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(articles) { article ->
            ArticleCard(article = article, onClick = onArticleClick )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArticleCard(article: Article, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(article.url) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            article.urlToImage?.let { imageUrl ->
                GlideImage(
                    model = imageUrl,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = article.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            article.description?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.source.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = article.publishedAt.split("T")[0],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
