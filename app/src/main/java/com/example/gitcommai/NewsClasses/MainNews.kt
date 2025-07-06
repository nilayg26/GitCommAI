package org.example.gitcommai.NewsClasses

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class MainNews(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)