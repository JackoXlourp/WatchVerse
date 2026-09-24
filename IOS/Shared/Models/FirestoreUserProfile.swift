//
//  FirestoreUserProfile.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-20.
//

import Foundation

struct FirestoreUserProfile {

    let uid: String

    var displayName: String
    var email: String

    var joinedDate: Int64
    var isFounder: Bool

    var showReleaseYears: Bool
    var notifyNewUniverses: Bool
    var currentUniverseID: String?
    
    var selectedUniverseFilters: [String: [String]]
    var journeyPositions: [String: String]

    var watchedMovies: [String]
    var skippedMovies: [String]

    var unlockedBadges: [String]
    var shownBadgePopups: [String]

    var schemaVersion: Int

    var cloudKitMigrationVersion: Int?
    var legacyCloudKitRecordID: String?
    
    var firestoreData: [String: Any] {

        var data: [String: Any] = [
            "uid": uid,
            "displayName": displayName,
            "email": email,
            "joinedDate": joinedDate,
            "isFounder": isFounder,
            "showReleaseYears": showReleaseYears,
            "notifyNewUniverses": notifyNewUniverses,
            "selectedUniverseFilters": selectedUniverseFilters,
            "journeyPositions": journeyPositions,
            "watchedMovies": watchedMovies,
            "skippedMovies": skippedMovies,
            "unlockedBadges": unlockedBadges,
            "shownBadgePopups": shownBadgePopups,
            "schemaVersion": schemaVersion
        ]
        
        if let currentUniverseID {
            data["currentUniverseID"] = currentUniverseID
        }

        if let cloudKitMigrationVersion {
            data["cloudKitMigrationVersion"] =
                cloudKitMigrationVersion
        }

        if let legacyCloudKitRecordID {
            data["legacyCloudKitRecordID"] =
                legacyCloudKitRecordID
        }

        return data
    }
}
