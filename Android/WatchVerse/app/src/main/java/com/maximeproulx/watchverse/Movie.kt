package com.maximeproulx.watchverse

data class Movie(
    val id: String,
    val title: String,
    val poster: String,
    val year: Int,
    val runtime: String,
    val synopsis: String,
    val director: String,
    val genres: List<String>,
    val tags: List<String> = emptyList(),
    val releaseStatus: String = "released",
    var isWatched: Boolean = false,
    var isSkipped: Boolean = false
)