package org.example.gitcommai.NewsClasses

import kotlinx.serialization.Serializable

@Serializable
data class MainNews(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)