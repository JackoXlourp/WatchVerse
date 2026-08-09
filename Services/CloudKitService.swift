//
//  CloudKitService.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-09.
//

import CloudKit
import Foundation

@Observable
final class CloudKitService {

    let database = CKContainer.default().privateCloudDatabase

    func findOrCreateUser(
        id: String,
        name: String,
        completion: @escaping (User) -> Void
    ) {

        let recordID = CKRecord.ID(recordName: id)

        database.fetch(withRecordID: recordID) { record, error in

            if record != nil {

                let user = User(
                    userID: id,
                    displayName: record?["displayName"] as? String ?? name,
                    joinedDate: record?["joinedDate"] as? Date ?? Date(),
                    isFounder: record?["isFounder"] as? Bool ?? false,
                    watchedMovies: record?["watchedMovies"] as? [String] ?? [],
                    settings: UserSettings(
                        showReleaseYears: record?["showReleaseYears"] as? Bool ?? true
                    )
                )

                completion(user)
                return
            }

            let record = CKRecord(
                recordType: "User",
                recordID: recordID
            )

            record["userID"] = id
            record["displayName"] = name
            record["joinedDate"] = Date()
            record["isFounder"] = true
            record["watchedMovies"] = [] as [String]
            record["showReleaseYears"] = true

            self.database.save(record) { _, error in

                if let error {
                    print("❌ CloudKit save failed:", error.localizedDescription)
                } else {
                    let user = User(
                        userID: id,
                        displayName: name,
                        joinedDate: record["joinedDate"] as! Date,
                        isFounder: true,
                        watchedMovies: [],
                        settings: UserSettings(showReleaseYears: true)
                    )

                    completion(user)

                    print("✅ New user created")
                }
            }
        }

    }
    private func makeUser(from record: CKRecord) -> User {

        User(
            userID: record["userID"] as? String ?? "",
            displayName: record["displayName"] as? String ?? "",
            joinedDate: record["joinedDate"] as? Date ?? .now,
            isFounder: record["isFounder"] as? Bool ?? false,
            watchedMovies: record["watchedMovies"] as? [String] ?? [],
            settings: UserSettings(
                showReleaseYears: record["showReleaseYears"] as? Bool ?? true
            )
        )
    }

    private func makeRecord(from user: User) -> CKRecord {

        let record = CKRecord(
            recordType: "User",
            recordID: CKRecord.ID(recordName: user.userID)
        )

        record["userID"] = user.userID
        record["displayName"] = user.displayName
        record["joinedDate"] = user.joinedDate
        record["isFounder"] = user.isFounder
        record["showReleaseYears"] = user.settings.showReleaseYears

        if !user.watchedMovies.isEmpty {
            record["watchedMovies"] = user.watchedMovies
        }

        return record
    }
}
