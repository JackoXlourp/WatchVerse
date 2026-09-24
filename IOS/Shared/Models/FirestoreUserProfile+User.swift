//
//  FirestoreUserProfile+User.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-21.
//

import Foundation

extension FirestoreUserProfile {

    var user: User {

        User(
            userID: uid,

            displayName:
                displayName,

            joinedDate:
                Date(
                    timeIntervalSince1970:
                        TimeInterval(joinedDate) / 1000
                ),

            isFounder:
                isFounder,

            watchedMovies:
                watchedMovies,

            skippedMovies:
                skippedMovies,

            unlockedBadges:
                unlockedBadges,

            settings: UserSettings(
                showReleaseYears: showReleaseYears,
                currentUniverseID: currentUniverseID,
                selectedUniverseFilters:
                    selectedUniverseFilters.mapValues { Set($0) },
                journeyPositions: journeyPositions
            ),

            shownBadgePopups:
                shownBadgePopups
        )
    }
}
