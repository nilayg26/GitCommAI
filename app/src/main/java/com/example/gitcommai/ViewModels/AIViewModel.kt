package com.example.gitcommai.ViewModels

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.gitcommai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

class AIViewModel(private val sharedPreferences: SharedPreferences) :State, ViewModel() {
    override val currentState: MutableState<String> = mutableStateOf("idle")
    private val apiKey= BuildConfig.apiKey
    private val generativeModel by lazy {
        GenerativeModel(modelName = "gemma-3n-e4b-it", apiKey = apiKey)
    }
    suspend fun getAIResponse(str:String):String{
        currentState.value=AIState.Loading
        return try {
            val response= generativeModel.generateContent(prompt = "$str, Keep all your responses concise and to the point, use emojis in ur response").text.toString()
            currentState.value= (AIState.Initalised)
            response
        }
        catch (e:Exception){
            AIState.Error =e.message.toString()
            currentState.value=AIState.Error
            println(AIState.Error)
            "Something got wrong, Please retry!"
        }
    }
    suspend fun greetAI():String{
        currentState.value=AIState.Loading
        val name = sharedPreferences.getString("user_name","")?:""
        return try {
            val response= generativeModel.generateContent(prompt = "User name is: $name, Please Greet him with a tech-motivated thought, For your future responses remember: Act as a tech-focused assistant, use emojis instead of markdown").text.toString()
            currentState.value= (AIState.Initalised)
           response
        }
        catch (e:Exception){
            AIState.Error =e.message.toString()
            currentState.value=AIState.Error
            println(AIState.Error)
           "Something got wrong, Please retry!"
        }
    }
    fun getAvatarUrl():String{
        return sharedPreferences.getString("user_avatar","")?:""
    }
}
object AIState:Status{
    override var Loading: String="aiLoading"
    override var Initalised: String="aiInitialised"
    override var Error: String="aiError"
}