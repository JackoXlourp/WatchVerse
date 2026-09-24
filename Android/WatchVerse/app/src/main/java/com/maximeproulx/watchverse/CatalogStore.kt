package com.maximeproulx.watchverse

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CatalogStore(
    context: Context,
    private val service: FirestoreCatalogService = FirestoreCatalogService()
) {
    private val applicationContext = context.applicationContext
    private val contentCache = mutableMapOf<String, List<Movie>>()
    private val artworkPreloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var activeUniverse by mutableStateOf<Universe?>(null)
        private set

    var availableUniverses by mutableStateOf<List<Universe>>(emptyList())
        private set

    var comingSoonUniverses by mutableStateOf<List<Universe>>(emptyList())
        private set

    var badges by mutableStateOf<List<Badge>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadInitialContent(universeID: String?) {
        isLoading = true
        errorMessage = null
        activeUniverse = null

        try {
            coroutineScope {
                val universeRequest = universeID?.let { id ->
                    async { service.fetchUniverse(id) }
                }
                val availableRequest = async { service.fetchAvailableUniverses() }
                val comingSoonRequest = async { service.fetchComingSoonUniverses() }
                val badgesRequest = async { service.fetchBadges() }

                val universe = universeRequest?.await()
                val available = availableRequest.await()
                val comingSoon = comingSoonRequest.await()
                val loadedBadges = badgesRequest.await()

                activeUniverse = universe
                availableUniverses = available
                comingSoonUniverses = comingSoon
                badges = loadedBadges
                if (universe != null) {
                    contentCache[universe.id] = universe.movies
                }
                available.forEach { item -> contentCache[item.id] = item.movies }

                val importantArtwork = buildSet {
                    if (universe != null) {
                        add(universe.banner)
                        add(universe.poster)
                        universe.movies.forEach { movie -> add(movie.poster) }
                    }
                    available.forEach { item -> add(item.poster) }
                    comingSoon.forEach { item -> add(item.poster) }
                    loadedBadges.forEach { badge -> add(badge.artwork) }
                }

                preloadInBackground(importantArtwork)
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

    fun prepareAvailableUniverse(universeID: String): Universe? =
        availableUniverses.firstOrNull { it.id == universeID }

    fun preloadArtworkInBackground(universe: Universe) {
        preloadInBackground(
            buildSet {
                add(universe.banner)
                add(universe.poster)
                universe.movies.forEach { movie -> add(movie.poster) }
            }
        )
    }

    fun activateUniverse(universe: Universe) {
        activeUniverse = universe
        contentCache[universe.id] = universe.movies
    }

    fun clearActiveUniverse() {
        activeUniverse = null
    }

    private fun preloadInBackground(sources: Set<String>) {
        artworkPreloadScope.launch {
            preloadSources(sources)
        }
    }

    private suspend fun preloadSources(sources: Set<String>) {
        sources.chunked(3).forEach { batch ->
            coroutineScope {
                batch.map { source ->
                    async { ArtworkRepository.preload(applicationContext, source) }
                }.awaitAll()
            }
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
