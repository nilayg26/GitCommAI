package com.example.gitcommai.ViewModels

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class AnimationViewModel(private val sharedPreferences: SharedPreferences):MainModel() {
    override val client: HttpClient = HttpClient(CIO)
    override var currentState: MutableState<String> = mutableStateOf("")
    private val url="https://nilayg26.github.io/Animation/"
    val currentAnimation= mutableStateOf("")
    private val map:Map<String,String> = mapOf(Pair("loading","loading_gitcommai.json"),Pair("login","login_gitcommai.json"),Pair("extra","account_gitcommai.json"))
    override suspend fun getData(key:String):String {
        currentAnimation.value=""
        val mainUrl=url+map[key]
        currentState.value="loading"
        var str=sharedPreferences.getString(key,"")?:""
        if (key=="login"){
            currentAnimation.value=str
        }
        if (str.isEmpty()) {
            str = client.get(mainUrl).bodyAsText()
            if(key=="login"){
                currentAnimation.value=str
            }
            sharedPreferences.edit().putString(key, str).apply()
        }
        currentState.value="retrieved"
        return str
    }
}