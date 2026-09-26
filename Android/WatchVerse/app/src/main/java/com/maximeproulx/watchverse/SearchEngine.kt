package com.maximeproulx.watchverse

import java.text.Normalizer
import java.util.Locale

data class SearchResultGroup(
    val universe: Universe,
    val universeMatches: Boolean,
    val content: List<Movie>,
    val badges: List<Badge>
)

data class SearchSuggestion(
    val universe: Universe,
    val content: Movie? = null,
    val badge: Badge? = null,
    val rank: Int
) {
    val id: String = when {
        content != null -> "content-${universe.id}-${content.id}"
        badge != null -> "badge-${badge.id}"
        else -> "universe-${universe.id}"
    }
}

object SearchEngine {
    fun results(
        query: String,
        universes: List<Universe>,
        badges: List<Badge>
    ): List<SearchResultGroup> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return emptyList()

        val searchableUniverses = if (badges.any { it.universeID == "watchverse" }) {
            universes + Universe(
                id = "watchverse",
                title = "WATCHVERSE",
                subtitle = "",
                fullTitle = "WATCHVERSE",
                description = "",
                banner = "",
                poster = ""
            )
        } else universes

        return searchableUniverses.mapNotNull { universe ->
            val universeRank = if (universe.id == "watchverse") null else rank(
                title = universe.title,
                alternateTitles = listOf(universe.fullTitle),
                metadata = listOf(universe.subtitle),
                query = normalizedQuery
            )
            val matchingContent = universe.movies.mapNotNull { movie ->
                rank(
                    title = movie.title,
                    tags = movie.tags,
                    genres = movie.genres,
                    query = normalizedQuery
                )?.let { movie to it }
            }.sortedWith(compareBy<Pair<Movie, Int>> { it.second }.thenBy { it.first.timelineOrder })
            val matchingBadges = badges.filter { it.universeID == universe.id }
                .mapNotNull { badge ->
                    rank(title = badge.title, query = normalizedQuery)?.let { badge to it }
                }.sortedBy { it.second }

            if (universeRank == null && matchingContent.isEmpty() && matchingBadges.isEmpty()) {
                null
            } else {
                val group = SearchResultGroup(
                    universe = universe,
                    universeMatches = universeRank != null,
                    content = matchingContent.map { it.first },
                    badges = matchingBadges.map { it.first }
                )
                group to listOfNotNull(
                    universeRank,
                    matchingContent.firstOrNull()?.second,
                    matchingBadges.firstOrNull()?.second
                ).min()
            }
        }.sortedWith(
            compareBy<Pair<SearchResultGroup, Int>> { it.second }
                .thenBy { it.first.universe.fullTitle }
        ).map { it.first }
    }

    fun suggestions(
        query: String,
        universes: List<Universe>,
        badges: List<Badge>
    ): List<SearchSuggestion> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return emptyList()

        return results(query, universes, badges).flatMap { group ->
            buildList {
                if (group.universe.id != "watchverse") {
                    rank(
                        title = group.universe.title,
                        alternateTitles = listOf(group.universe.fullTitle),
                        metadata = listOf(group.universe.subtitle),
                        query = normalizedQuery
                    )?.let { add(SearchSuggestion(group.universe, rank = it)) }
                }
                group.content.forEach { movie ->
                    rank(
                        title = movie.title,
                        tags = movie.tags,
                        genres = movie.genres,
                        query = normalizedQuery
                    )?.let { add(SearchSuggestion(group.universe, content = movie, rank = it)) }
                }
                group.badges.forEach { badge ->
                    rank(title = badge.title, query = normalizedQuery)?.let {
                        add(SearchSuggestion(group.universe, badge = badge, rank = it))
                    }
                }
            }
        }.sortedWith(compareBy<SearchSuggestion> { it.rank }.thenBy { it.id }).take(6)
    }

    private fun normalize(value: String): String = Normalizer.normalize(
        value.trim(), Normalizer.Form.NFD
    ).replace(Regex("\\p{M}+"), "").lowercase(Locale.getDefault())

    private fun rank(
        title: String,
        alternateTitles: List<String> = emptyList(),
        metadata: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        genres: List<String> = emptyList(),
        query: String
    ): Int? {
        val titles = (listOf(title) + alternateTitles).map(::normalize)
        return when {
            titles.any { it == query } -> 0
            titles.any { it.startsWith(query) } -> 1
            titles.any { it.contains(query) } -> 2
            (tags + genres).map(::normalize).any { it.contains(query) } -> 3
            metadata.map(::normalize).any { it.contains(query) } -> 4
            else -> null
        }
    }
}
