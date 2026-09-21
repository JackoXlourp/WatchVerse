package com.maximeproulx.watchverse

data class WatchVerseUser(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val joinedDate: Long = 0L,
    val isFounder: Boolean = false,
    val showReleaseYears: Boolean = true,
    val notifyNewUniverses: Boolean = true,
    val selectedUniverseFilters: Map<String, List<String>> = emptyMap(),
    val journeyPositions: Map<String, String> = emptyMap(),
    val unlockedBadges: List<String> = emptyList(),
    val shownBadgePopups: List<String> = emptyList(),
    val watchedMovies: List<String> = emptyList(),
    val skippedMovies: List<String> = emptyList(),
    val schemaVersion: Long = 1L,
    val cloudKitMigrationVersion: Long? = null,
    val legacyCloudKitRecordID: String? = null
)
