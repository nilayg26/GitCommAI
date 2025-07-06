package com.example.gitcommai.ViewModels

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.gitcommai.RepoClasses.ReposItem
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class User(
    val login:String="",
    val html_url:String?=null,
    var name:String?=null,
    val avatar_url:String?=null,
    val public_repos:Int?=null,
    val followers:Int=0,
    val following:Int=0,
    val repos_url:String?=null,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(login)
        parcel.writeString(html_url)
        parcel.writeString(name)
        parcel.writeString(avatar_url)
        parcel.writeValue(public_repos)
        parcel.writeInt(followers)
        parcel.writeInt(following)
        parcel.writeString(repos_url)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)
        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }
}
class AuthViewModel(private var sharedPreferences: SharedPreferences) :MainModel(){
    override var currentState: MutableState<String> = mutableStateOf("idle")
    private var repoState: MutableState<String> = mutableStateOf("idle")
    private val firebaseAuth by lazy {  FirebaseAuth.getInstance()}
    override suspend fun getData(key: String): String {
       return ""
    }
    override val client:HttpClient by lazy {   HttpClient(CIO)}
    private var _user=User()
    suspend fun getUser():User{
        try {
            if (_user.login.isNotBlank()) {
                return _user.copy()
            } else {
                val token = sharedPreferences.getString("access_token", "") ?: ""
                _user = Json.decodeFromString(getUser(token))
                return _user
            }
        }
        catch (e:Exception){
            println("From get user:"+e.message)
            return User()
        }
    }
   @SuppressLint("SuspiciousIndentation")
   fun login(activity: Activity) {
       currentState.value = "loading"
       val provider = OAuthProvider.newBuilder("github.com")
       provider.scopes = listOf("repo")

       val pendingResultTask = firebaseAuth.pendingAuthResult
       if (pendingResultTask != null) {
           println("Found pending auth result")
           handleLoginResult(pendingResultTask)
       } else {
           println("Login was started")
           firebaseAuth.startActivityForSignInWithProvider(activity, provider.build())
               .addOnSuccessListener {
                   handleLoginResult(Tasks.forResult(it))
               }
               .addOnFailureListener {
                   println("Login failed: ${it.message}")
                   currentState.value = "error"
               }
       }
   }

    private fun handleLoginResult(resultTask: Task<AuthResult>) {
        resultTask
            .addOnSuccessListener {
                println("Reached completion")
                val credential = it.credential as? OAuthCredential
                val token = credential?.accessToken ?: ""
                sharedPreferences.edit().putString("access_token", token).apply()

                viewModelScope.launch {
                    try {
                        val userJson = getUser(token)
                        _user = Json.decodeFromString(userJson)
                        ChatViewModel.checkOrRegisterUser(_user)
                        val displayName = _user.name ?: _user.login
                        sharedPreferences.edit().putString("user_name", displayName).apply()
                        sharedPreferences.edit().putString("user_id", _user.login).apply()
                        sharedPreferences.edit().putString("user_avatar", _user.avatar_url).apply()
                        currentState.value = "access_token_retrieved"
                    } catch (e: Exception) {
                        Log.e("GitHubUser", "Failed to fetch user", e)
                        currentState.value = "error"
                    }
                }
            }
            .addOnFailureListener {
                println("Login failed (inside handler): ${it.message}")
                currentState.value = "error"
            }
    }

    private suspend fun getUser(token: String):String{
        return try {
            client.get("https://api.github.com/user"){
                headers{
                    append("Authorization","Bearer $token")
                }
            }.bodyAsText()
        } catch (e:Exception){
            println(e.message.toString())
            ""
        }
    }
    suspend fun getRepos(): List<ReposItem>? {
        repoState.value="loading"
        try {
            val json = _user.repos_url?.let {
                client.get("https://api.github.com/user/repos") {
                    url {
                        parameters.append("sort", "updated")
                        parameters.append("visibility", "all")
                    }
                    headers {
                        append(
                            "Authorization",
                            "Bearer ${sharedPreferences.getString("access_token", "")}"
                        )
                    }
                }.bodyAsText()
            } ?: ""
            repoState.value="retrieved"
            return Json.decodeFromString<List<ReposItem>>(json)
        }
        catch (e:Exception){
            println(e.printStackTrace())
            return null
        }
    }
    fun signOut(chatViewModel: ChatViewModel){
        chatViewModel.signOut()
        _user=User()
        sharedPreferences.edit().clear().apply()
        firebaseAuth.signOut()
        currentState.value="signOut"
    }

}