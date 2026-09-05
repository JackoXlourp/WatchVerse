package com.maximeproulx.watchverse

data class BadgeUnlockResult(
    val user: WatchVerseUser,
    val newlyUnlockedBadges: List<Badge>
)

object BadgeUnlockEvaluator {

    fun check(user: WatchVerseUser): BadgeUnlockResult {
        val newlyUnlockedBadges = BadgeData.all.filter { badge ->
            badge.requiredMovieIDs.isNotEmpty() &&
                    !user.unlockedBadges.contains(badge.id) &&
                    badge.requiredMovieIDs.all { movieID ->
                        user.watchedMovies.contains(movieID)
                    }
        }

        return BadgeUnlockResult(
            user = user.copy(
                unlockedBadges =
                    user.unlockedBadges + newlyUnlockedBadges.map { badge -> badge.id }
            ),
            newlyUnlockedBadges = newlyUnlockedBadges
        )
    }
}
