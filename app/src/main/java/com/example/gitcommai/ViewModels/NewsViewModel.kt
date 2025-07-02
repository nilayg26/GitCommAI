package com.example.gitcommai.ViewModels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.gitcommai.BuildConfig
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.gitcommai.NewsClasses.MainNews

class NewsViewModel:MainModel() {
    override var currentState: MutableState<String> = mutableStateOf("")
    override val client = HttpClient(CIO){
        install(HttpTimeout) {
            requestTimeoutMillis = 15000  // Total time allowed for a request
            connectTimeoutMillis = 10000  // Time to establish a connection
            socketTimeoutMillis = 15000   // Time waiting for data
        }
    }
    private val apiKey= BuildConfig.newsApiKey
    var mainNews: MutableState<MainNews?> = mutableStateOf(null)
        private set
    override suspend fun getData(key: String): String {
        return withContext(Dispatchers.IO) {
            try {
                currentState.value = NewsStatus.Loading
                println(apiKey)
                val response = client.get("https://nilayg26.github.io/Animation/news_gitcommai.json")
                println("Status: ${response.status}")
                //println("Body: ${response.bodyAsText()}")
                if (response.status.isSuccess()) {
                    mainNews.value = Gson().fromJson(response.bodyAsText(), MainNews::class.java)
                    currentState.value = NewsStatus.Initalised
                } else {
                    currentState.value = NewsStatus.Error
                }
            } catch (e: Exception) {
                println("Exception: ${e.localizedMessage}")
                e.printStackTrace()
                currentState.value = NewsStatus.Error
            }
            ""
        }
    }
}
interface Status{
    var Loading:String
    var Initalised:String
    var Error:String
}
object NewsStatus:Status{
    override var Loading: String="NewsLoading"
    override var Initalised: String="NewsInitialised"
    override var Error: String="NewsError"
}