package com.maximeproulx.watchverse

data class Badge(
    val id: String,
    val title: String,
    val universeID: String,
    val universeTitle: String,
    val artwork: String,
    val description: String,
    val requiredContentIDs: List<String>,
    val sortOrder: Int,
    val isUnlocked: Boolean = false
)
