package com.maximeproulx.watchverse

data class WatchVerseUser(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val joinedDate: Long = 0L,
    val isFounder: Boolean = false,
    val showReleaseYears: Boolean = true,
    val notifyNewUniverses: Boolean = true,
    val unlockedBadges: List<String> = emptyList(),
    val shownBadgePopups: List<String> = emptyList(),
    val watchedMovies: List<String> = emptyList(),
    val skippedMovies: List<String> = emptyList()
)