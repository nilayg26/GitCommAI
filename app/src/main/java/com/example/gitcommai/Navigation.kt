package com.example.gitcommai

interface Navigation{
    val route: String
}
object LoginPage:Navigation{
    override val route: String = "Login"
}
object AccountPage :Navigation{
    override val route: String = "Acc"
}
object AIPage:Navigation {
    override val route: String = "AI"
}
object ChatPage:Navigation{
    override val route: String = "Chat"
}
object NewsPage:Navigation {
    override val route: String = "News"
}
object ChatMessage:Navigation {
    override val route: String = "ChatMsg"
}

