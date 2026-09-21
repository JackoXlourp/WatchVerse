package com.maximeproulx.watchverse

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreCatalogService(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun fetchUniverse(universeID: String): Universe {
        val document = db.collection("universes")
            .document(universeID)
            .get()
            .awaitResult()

        require(document.exists()) {
            "Universe $universeID does not exist"
        }

        return document.toUniverse(
            movies = fetchContent(universeID)
        )
    }

    suspend fun fetchComingSoonUniverses(): List<Universe> {
        val snapshot = db.collection("universes")
            .orderBy("sortOrder")
            .get()
            .awaitResult()

        return snapshot.documents
            .filter { document -> document.getString("state") == "comingSoon" }
            .map { document -> document.toUniverse() }
    }

    suspend fun fetchContent(universeID: String): List<Movie> {
        val snapshot = db.collection("universes")
            .document(universeID)
            .collection("content")
            .orderBy("timelineOrder")
            .get()
            .awaitResult()

        return snapshot.documents.map { document ->
            Movie(
                id = document.getString("id") ?: document.id,
                title = document.requiredString("title"),
                poster = document.requiredString("poster"),
                year = document.getLong("year")?.toInt() ?: 0,
                runtime = document.requiredString("runtime"),
                synopsis = document.requiredString("synopsis"),
                director = document.requiredString("director"),
                genres = document.stringList("genres"),
                tags = document.stringList("tags"),
                releaseStatus = document.getString("releaseStatus") ?: "released",
                type = document.getString("type") ?: "movie",
                timelineOrder = document.getLong("timelineOrder")?.toInt() ?: 0
            )
        }
    }

    suspend fun fetchBadges(): List<Badge> {
        val snapshot = db.collection("badges")
            .get()
            .awaitResult()

        return snapshot.documents.map { document ->
            Badge(
                id = document.getString("id") ?: document.id,
                title = document.requiredString("title"),
                universeID = document.requiredString("universeID"),
                universeTitle = document.requiredString("universeTitle"),
                artwork = document.requiredString("artwork"),
                description = document.requiredString("description"),
                requiredContentIDs = document.stringList("requiredContentIDs"),
                sortOrder = document.getLong("sortOrder")?.toInt() ?: 0
            )
        }.sortedWith(
            compareBy<Badge> { badge ->
                if (badge.universeID == "watchverse") 0 else 1
            }.thenBy { badge -> badge.universeTitle }
                .thenBy { badge -> badge.sortOrder }
        )
    }

    private fun DocumentSnapshot.toUniverse(
        movies: List<Movie> = emptyList()
    ): Universe {
        return Universe(
            id = getString("id") ?: id,
            state = getString("state") ?: "comingSoon",
            title = requiredString("title"),
            subtitle = requiredString("subtitle"),
            fullTitle = requiredString("fullTitle"),
            description = requiredString("description"),
            logo = getString("logo").orEmpty(),
            banner = requiredString("banner"),
            poster = requiredString("poster"),
            filters = stringList("filters"),
            sortOrder = getLong("sortOrder")?.toInt() ?: 0,
            movies = movies
        )
    }

    private fun DocumentSnapshot.requiredString(field: String): String {
        return requireNotNull(getString(field)) {
            "${reference.path} is missing $field"
        }
    }

    private fun DocumentSnapshot.stringList(field: String): List<String> {
        return (get(field) as? List<*>)
            ?.filterIsInstance<String>()
            .orEmpty()
    }
}

private suspend fun <T> Task<T>.awaitResult(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
        addOnFailureListener { error ->
            if (continuation.isActive) {
                continuation.resumeWithException(error)
            }
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}
