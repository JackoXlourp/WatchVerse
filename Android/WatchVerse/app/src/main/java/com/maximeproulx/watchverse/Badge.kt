package com.maximeproulx.watchverse

data class Badge(
    val id: String,
    val title: String,
    val universe: String,
    val imageName: String,
    val description: String,
    val isUnlocked: Boolean,
    val requiredMovieIDs: List<String>
)