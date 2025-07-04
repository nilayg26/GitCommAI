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
import com.example.gitcommai.RepoClasses.Repos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch

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
    private val gson by lazy {  Gson()}
    suspend fun getUser():User{
        try {
            if (_user.login.isNotBlank()) {
                return _user.copy()
            } else {
                val token = sharedPreferences.getString("access_token", "") ?: ""
                _user = gson.fromJson(getUser(token), User::class.java)
                return _user
            }
        }
        catch (e:Exception){
            println(e.message)
            return User()
        }
    }
   @SuppressLint("SuspiciousIndentation")
   fun login(activity: Activity){
        currentState.value="loading"
        val provider= OAuthProvider.newBuilder("github.com")
           firebaseAuth.startActivityForSignInWithProvider(activity, provider.build())
               .addOnSuccessListener {
                   if (it != null) {
                       val credential = it.credential as OAuthCredential
                       val token = credential.accessToken ?: ""
                       sharedPreferences.edit().putString("access_token", token).apply()
                       viewModelScope.launch {
                           try {
                               val userJson = getUser(token)
                               _user = gson.fromJson(userJson, User::class.java)
                               ChatViewModel.checkOrRegisterUser(_user)
                               if (!_user.name.isNullOrBlank()) {
                                   sharedPreferences.edit().putString("user_name", _user.name)
                                       .apply()
                               }
                               else{
                                   _user.name=_user.login
                                   sharedPreferences.edit().putString("user_name", _user.login)
                                       .apply()
                               }
                               sharedPreferences.edit().putString("user_id",_user.login).apply()
                               sharedPreferences.edit().putString("user_avatar",_user.avatar_url).apply()
                               currentState.value = "access_token_retrieved"
                           } catch (e: Exception) {
                               Log.e("GitHubUser", "Failed to fetch user", e)
                               currentState.value = "error"
                           }
                       }
                   } else {
                       println("GOT NULL")
                       currentState.value = "error"
                   }
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
    suspend fun getRepos(): Repos? {
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
            return gson.fromJson(json, Repos::class.java)
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