package com.example.gitcommai.ViewModels

import androidx.compose.runtime.MutableState
import io.ktor.client.HttpClient

interface State {
    val currentState : MutableState<String>
}