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

        let selectedUniverseFilters: [String: [String]]

        if let perUniverseFilters =
            data["selectedUniverseFilters"] as? [String: [String]] {

            selectedUniverseFilters = perUniverseFilters
        } else if let legacyFilters =
            data["selectedUniverseFilters"] as? [String] {

            selectedUniverseFilters = ["mcu": legacyFilters]
        } else {
            selectedUniverseFilters = [:]
        }

        return FirestoreUserProfile(
            uid: uid,
            displayName: data["displayName"] as? String ?? "",
            email: data["email"] as? String ?? "",
            joinedDate: data["joinedDate"] as? Int64 ?? 0,
            isFounder: data["isFounder"] as? Bool ?? false,
            showReleaseYears:
                data["showReleaseYears"] as? Bool ?? true,
            notifyNewUniverses:
                data["notifyNewUniverses"] as? Bool ?? true,
            currentUniverseID:
                data["currentUniverseID"] as? String,
            selectedUniverseFilters: selectedUniverseFilters,
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
    
    func deleteProfile(
        uid: String
    ) async throws {

        try await db
            .collection("users")
            .document(uid)
            .delete()
    }
    
    func saveProfile(
        _ profile: FirestoreUserProfile
    ) async throws {

        try await db
            .collection("users")
            .document(profile.uid)
            .setData(
                profile.firestoreData,
                merge: true
            )
    }
    
    func addFCMToken(
        uid: String,
        token: String
    ) async throws {

        try await db
            .collection("users")
            .document(uid)
            .updateData([
                "fcmTokens": FieldValue.arrayUnion([token])
            ])
    }
    
    func removeFCMToken(
        uid: String,
        token: String
    ) async throws {

        try await db
            .collection("users")
            .document(uid)
            .updateData([
                "fcmTokens": FieldValue.arrayRemove([token])
            ])
    }

    func updateMutableProfile(
        _ profile: FirestoreUserProfile
    ) async throws {

        var mutableData: [String: Any] = [
            "displayName": profile.displayName,
            "showReleaseYears": profile.showReleaseYears,
            "notifyNewUniverses": profile.notifyNewUniverses,
            "selectedUniverseFilters": profile.selectedUniverseFilters,
            "journeyPositions": profile.journeyPositions,
            "watchedMovies": profile.watchedMovies,
            "skippedMovies": profile.skippedMovies,
            "unlockedBadges": profile.unlockedBadges,
            "shownBadgePopups": profile.shownBadgePopups
        ]

        if let currentUniverseID = profile.currentUniverseID {
            mutableData["currentUniverseID"] = currentUniverseID
        } else {
            mutableData["currentUniverseID"] = FieldValue.delete()
        }

        try await db
            .collection("users")
            .document(profile.uid)
            .setData(
                mutableData,
                merge: true
            )
    }
}
