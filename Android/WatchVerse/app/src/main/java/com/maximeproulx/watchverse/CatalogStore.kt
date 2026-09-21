package com.maximeproulx.watchverse

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CatalogStore(
    context: Context,
    private val service: FirestoreCatalogService = FirestoreCatalogService()
) {
    private val applicationContext = context.applicationContext
    private val contentCache = mutableMapOf<String, List<Movie>>()

    var activeUniverse by mutableStateOf<Universe?>(null)
        private set

    var comingSoonUniverses by mutableStateOf<List<Universe>>(emptyList())
        private set

    var badges by mutableStateOf<List<Badge>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadInitialContent(universeID: String = "mcu") {
        isLoading = true
        errorMessage = null

        try {
            coroutineScope {
                val universeRequest = async { service.fetchUniverse(universeID) }
                val comingSoonRequest = async { service.fetchComingSoonUniverses() }
                val badgesRequest = async { service.fetchBadges() }

                val universe = universeRequest.await()
                val comingSoon = comingSoonRequest.await()
                val loadedBadges = badgesRequest.await()

                activeUniverse = universe
                comingSoonUniverses = comingSoon
                badges = loadedBadges
                contentCache[universe.id] = universe.movies

                val importantArtwork = buildSet {
                    add(universe.banner)
                    comingSoon.forEach { item -> add(item.poster) }
                    loadedBadges.forEach { badge -> add(badge.artwork) }
                }

                importantArtwork.map { source ->
                    async {
                        ArtworkRepository.preload(applicationContext, source)
                    }
                }.awaitAll()
            }
        } catch (error: Exception) {
            android.util.Log.e(
                "WatchVerseCatalog",
                "Initial Firestore catalog load failed",
                error
            )
            errorMessage = error.localizedMessage ?: "Unable to load the WatchVerse catalog."
        } finally {
            isLoading = false
        }
    }

    suspend fun contentFor(universeID: String): List<Movie> {
        activeUniverse
            ?.takeIf { universe -> universe.id == universeID }
            ?.let { universe -> return universe.movies }

        contentCache[universeID]?.let { content -> return content }

        return try {
            service.fetchContent(universeID).also { content ->
                contentCache[universeID] = content
            }
        } catch (error: Exception) {
            android.util.Log.e(
                "WatchVerseCatalog",
                "Content load failed for $universeID",
                error
            )
            emptyList()
        }
    }
}
