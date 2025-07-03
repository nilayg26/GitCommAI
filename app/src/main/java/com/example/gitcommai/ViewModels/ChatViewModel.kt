package com.example.gitcommai.ViewModels
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val text: String="",
    val sender: String?=null,
    val time: Timestamp?=null
)
class ChatViewModel(private val sharedPreferences: SharedPreferences):State,ViewModel() {
    override var currentState: MutableState<String> = mutableStateOf("")
    val chatList: MutableList<LastMessage>  by lazy {  mutableStateListOf() }
    val messagesList:MutableList<ChatMessage> by lazy { mutableStateListOf() }
    private var currentChatRoomId:MutableState<String> =  mutableStateOf("")
    private var listener:ListenerRegistration?= null
    private var chatRoomListener:ListenerRegistration?=null
    private fun isAdmin(sender: String):Boolean{
        val userId= sharedPreferences.getString("user_id","")?:""
        return sender==userId
    }
    fun getOtherUserId(chatRoomId: String):String{
        val list= chatRoomId.split("_")
        return if (isAdmin(list[0])) list[1] else list[0]
    }
    fun getUserId():String{
        return sharedPreferences.getString("user_id","")?:""
    }
    fun setCurrentChatRoomId(chatRoomId: String){
        currentChatRoomId.value=chatRoomId
    }
    fun getCurrentChatRoomId():String{
        return currentChatRoomId.value
    }
    suspend fun getUser(userId:String):User?{
        return try {
            var user:User?=User()
            firestore.collection("users").document(userId).get().addOnSuccessListener {
                user = it.toObject(User::class.java)
            }.await()
            return user
        }
        catch (e:Exception){
            null
        }
    }
    companion object {
        private val firestore by lazy {
            FirebaseFirestore.getInstance()
        }
        var registrationState: MutableState<String> = mutableStateOf("notRegistered")
        private fun registerUser(user: User, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
            println("User going to firebase is: $user")
            val userDocRef = firestore.collection("users").document(user.login)
            userDocRef.set(user)
                .addOnSuccessListener {
                    registrationState.value="registered"
                    println("Nilayyyy : Here is registered value")
                }
                .addOnFailureListener {
                    e ->
                    println("Exception from registerUser()"+e.message.toString())
                }
        }
        fun checkOrRegisterUser(user:User){
            val userDocRef = firestore.collection("users").document(user.login)
            userDocRef.get().addOnSuccessListener {
                if (it.exists()) {
                    registrationState.value="registered"
                }
                else{
                    registerUser(user)
                }
            }.addOnFailureListener {
                e ->
                println("Exception from registerUser()"+e.message.toString())
            }
        }
    }
    fun searchUser(query:String,onFailure: (Exception) -> Unit={},onSuccess: (List<User>) -> Unit){
        currentState.value="loading"
        val userId= sharedPreferences.getString("user_id","")?:""
        firestore.collection("users").orderBy("login").startAt(query).endAt(query+"\uf8ff").whereNotEqualTo("login",userId).get().addOnSuccessListener {
            result->
            if (!result.isEmpty){
                currentState.value="searchUser"
                val users=result.documents.mapNotNull {  it.toObject(User::class.java)}
                onSuccess(users)
            }
            else{
                onSuccess(emptyList())
            }

        }.addOnFailureListener {
            e->
            println(e.message.toString())
        }
    }
    fun allUsers(onResult: (List<User>) -> Unit, onError: (Exception) -> Unit = {}) {
        currentState.value="loading"
        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                currentState.value="allUsers"
                val users = result.documents.mapNotNull { it.toObject(User::class.java) }
                onResult(users)
            }
            .addOnFailureListener { e -> onError(e) }
    }
    private suspend fun checkChatRoom(chatRoomId:String, reverseChatRoomId:String):Boolean{
        try {
            val docRef = firestore.collection("chatroom").document(chatRoomId).get().await()
            val reverseDocRef = firestore.collection("chatroom").document(reverseChatRoomId).get().await()
            return (!docRef.exists() && !reverseDocRef.exists())
        }
        catch (e:Exception){
            println("Error from checkUser()"+e.message.toString())
            return false
        }
    }
    fun addChatRoom(currentUser: User, otherUser: User, onCreated: (String) -> Unit = {}, onError: (Exception) -> Unit = {}) {
        currentState.value="loading"
        val chatRoomId = "${currentUser.login}_${otherUser.login}"
        val reverseChatRoomId = "${otherUser.login}_${currentUser.login}"
        viewModelScope.launch {
            if(checkChatRoom(chatRoomId, reverseChatRoomId)) {
                val chatRoomData = hashMapOf(
                    "users" to listOf(currentUser.login, otherUser.login),
                    "avatar_url" to listOf(currentUser.avatar_url, otherUser.avatar_url),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "text" to "",
                    "sender" to "",
                    "time" to Timestamp.now(),
                    "roomId" to chatRoomId
                )
                val docRef = firestore.collection("chatroom").document(chatRoomId)
                docRef.set(chatRoomData)
                    .addOnSuccessListener {
                        currentState.value = "chatRoomAdded"
                        val userChatRef1 = firestore.collection("users").document(currentUser.login)
                            .collection("chatroom").document(chatRoomId)
                        val userChatRef2 = firestore.collection("users").document(otherUser.login)
                            .collection("chatroom").document(chatRoomId)
                        val metadata = mapOf("ref" to docRef.path)
                        sharedPreferences.edit().putString(chatRoomId,otherUser.login).apply()
                        userChatRef1.set(metadata)
                        userChatRef2.set(metadata)
                        onCreated(chatRoomId)
                    }
                    .addOnFailureListener { e -> onError(e) }
            }
            else{
                Log.d("Exists","User already exists")
            }
        }
    }
    @SuppressLint("SuspiciousIndentation")
    private fun getChatUserIdAndLastMessage(chatRoomId: String, onSuccess: (LastMessage) -> Unit){
        val chatRoomRef = firestore.collection("chatroom").document(chatRoomId)
        chatRoomRef.addSnapshotListener {
                result , _->
            try {
                if (result != null) {
                    if(result.exists()) {
                        val lastMessage = result.toObject(LastMessage::class.java)
                        if (lastMessage != null) {
                            onSuccess(lastMessage)
                        }
                    }
                }
            }
            catch (e:Exception){
                println("Exception from getChatUserIdAndLastMessage()"+e.message.toString())
            }
        }
    }
    fun getChatRoomsSnapShot(userId: String){
        currentState.value = "loading"
        chatRoomListener?.remove()
        chatRoomListener= firestore.collection("users").document(userId).collection("chatroom")
            .addSnapshotListener { result, _ ->
                result?.documentChanges?.forEach { it ->
                    if (it.type==DocumentChange.Type.ADDED){
                        getChatUserIdAndLastMessage(chatRoomId = it.document.id) { lastMessage ->
                            val chat= chatList.find {chats-> chats.roomId==lastMessage.roomId }
                            if (chat==null) {
                                chatList.add(
                                    lastMessage
                                )
                            }
                            else{
                                chatList.remove(chat)
                                chatList.add(lastMessage)
                            }
                        }
                    }
                }
                currentState.value = "chatRoom"
            }
    }
    fun getAllMessageSnapShot(chatRoomId: String){
        currentState.value="loading"
        messagesList.clear()
        listener?.remove()
        listener= firestore.collection("chatroom").document(chatRoomId).collection("message").orderBy("time").addSnapshotListener {result , _ ->
            result?.documentChanges?.forEach { change ->
                if (change.type == DocumentChange.Type.ADDED) {
                    val message = change.document.toObject(ChatMessage::class.java)
                    messagesList.add(message)
                }
                else if (change.type==DocumentChange.Type.REMOVED){
                    val message=change.document.toObject(ChatMessage::class.java)
                    messagesList.remove(message)
                }
            }
            currentState.value="messagesRetrieved"
        }
    }
    fun sendMessage(chatRoomId: String, message: ChatMessage, onSent: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        currentState.value="loading"
        val docRef=firestore.collection("chatroom").document(chatRoomId)
        docRef.set(message, SetOptions.merge())
        val messageRef = docRef.collection("message").document()
        messageRef.set(message)
            .addOnSuccessListener { currentState.value="delivered";onSent() }
            .addOnFailureListener { e -> onError(e) }
    }
}
data class LastMessage(
    var text: String?=null,
    var sender: String?=null,
    var time: Timestamp?=null,
    val users: List<String> = emptyList(),
    var avatar_url:List<String> = emptyList(),
    val createdAt: Timestamp?=null,
    val roomId:String?=null
)