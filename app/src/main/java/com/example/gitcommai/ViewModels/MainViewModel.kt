package com.example.gitcommai.ViewModels

import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient

abstract class MainModel(): ViewModel() , State{
    abstract suspend fun getData(key:String=""):String
    abstract val client: HttpClient
}