//
//  CloudKitMigrationService.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-20.
//

import Foundation
import FirebaseFunctions

struct CloudKitMigrationService {

    private let functions = Functions.functions(
        region: "northamerica-northeast1"
    )

    func migrate(
        profile: FirestoreUserProfile
    ) async throws {

        let payload: [String: Any] = [
            "legacyCloudKitRecordID":
                profile.legacyCloudKitRecordID ?? "",

            "displayName":
                profile.displayName,

            "joinedDate":
                profile.joinedDate,

            "isFounder":
                profile.isFounder,

            "showReleaseYears":
                profile.showReleaseYears,

            "notifyNewUniverses":
                profile.notifyNewUniverses,

            "selectedUniverseFilters":
                profile.selectedUniverseFilters,

            "journeyPositions":
                profile.journeyPositions,

            "watchedMovies":
                profile.watchedMovies,

            "skippedMovies":
                profile.skippedMovies,

            "unlockedBadges":
                profile.unlockedBadges,

            "shownBadgePopups":
                profile.shownBadgePopups
        ]

        _ = try await functions
            .httpsCallable("migrateCloudKitUser")
            .call(payload)
    }
}
