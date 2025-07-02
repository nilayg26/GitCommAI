package org.example.gitcommai.NewsClasses

import kotlinx.serialization.Serializable

@Serializable
data class Source(
    val id: String?,
    val name: String
)