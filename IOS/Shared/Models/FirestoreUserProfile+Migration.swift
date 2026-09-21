//
//  FirestoreUserProfile+Migration.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-20.
//

import Foundation

extension FirestoreUserProfile {

    static func migrated(
        from legacyUser: User,
        firebaseUID: String,
        email: String,
        notifyNewUniverses: Bool
    ) -> FirestoreUserProfile {

        FirestoreUserProfile(
            uid: firebaseUID,
            displayName: legacyUser.displayName,
            email: email,
            joinedDate: Int64(
                legacyUser.joinedDate.timeIntervalSince1970 * 1000
            ),
            isFounder: legacyUser.isFounder,
            showReleaseYears:
                legacyUser.settings.showReleaseYears,
            notifyNewUniverses:
                notifyNewUniverses,
            selectedUniverseFilters:
                legacyUser.settings.selectedUniverseFilters
                    .mapValues { Array($0) },
            journeyPositions:
                legacyUser.settings.journeyPositions,
            watchedMovies:
                legacyUser.watchedMovies,
            skippedMovies:
                legacyUser.skippedMovies,
            unlockedBadges:
                legacyUser.unlockedBadges,
            shownBadgePopups:
                legacyUser.shownBadgePopups,
            schemaVersion: 2,
            cloudKitMigrationVersion: 1,
            legacyCloudKitRecordID:
                legacyUser.userID
        )
    }
}
