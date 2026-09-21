package com.maximeproulx.watchverse

data class Universe(
    val id: String,
    val state: String = "available",
    val title: String,
    val subtitle: String,
    val fullTitle: String,
    val description: String,
    val logo: String = "",
    val banner: String,
    val poster: String,
    val filters: List<String> = emptyList(),
    val sortOrder: Int = 0,
    val movies: List<Movie> = emptyList()
)
