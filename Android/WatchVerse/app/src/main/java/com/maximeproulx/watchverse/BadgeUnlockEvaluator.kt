package com.maximeproulx.watchverse

data class BadgeUnlockResult(
    val user: WatchVerseUser,
    val newlyUnlockedBadges: List<Badge>
)

object BadgeUnlockEvaluator {

    fun check(
        user: WatchVerseUser,
        badges: List<Badge>
    ): BadgeUnlockResult {
        val newlyUnlockedBadges = badges.filter { badge ->
            badge.requiredContentIDs.isNotEmpty() &&
                    !user.unlockedBadges.contains(badge.id) &&
                    badge.requiredContentIDs.all { movieID ->
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
