//
//  FirestoreUserService.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-20.
//

import Foundation
import FirebaseFirestore

struct FirestoreUserService {

    private let db = Firestore.firestore()

    func fetchProfile(
        uid: String
    ) async throws -> FirestoreUserProfile? {

        let document = try await db
            .collection("users")
            .document(uid)
            .getDocument()

        guard document.exists,
              let data = document.data() else {
            return nil
        }

        let rawFilters =
            data["selectedUniverseFilters"] as? [String: [String]] ?? [:]

        return FirestoreUserProfile(
            uid: data["uid"] as? String ?? uid,
            displayName: data["displayName"] as? String ?? "",
            email: data["email"] as? String ?? "",
            joinedDate: data["joinedDate"] as? Int64 ?? 0,
            isFounder: data["isFounder"] as? Bool ?? false,
            showReleaseYears:
                data["showReleaseYears"] as? Bool ?? true,
            notifyNewUniverses:
                data["notifyNewUniverses"] as? Bool ?? true,
            selectedUniverseFilters: rawFilters,
            journeyPositions:
                data["journeyPositions"] as? [String: String] ?? [:],
            watchedMovies:
                data["watchedMovies"] as? [String] ?? [],
            skippedMovies:
                data["skippedMovies"] as? [String] ?? [],
            unlockedBadges:
                data["unlockedBadges"] as? [String] ?? [],
            shownBadgePopups:
                data["shownBadgePopups"] as? [String] ?? [],
            schemaVersion:
                data["schemaVersion"] as? Int ?? 1,
            cloudKitMigrationVersion:
                data["cloudKitMigrationVersion"] as? Int,
            legacyCloudKitRecordID:
                data["legacyCloudKitRecordID"] as? String
        )
    }
}
