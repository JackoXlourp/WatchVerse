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

    //MARK: findOrCreateUser
    func findOrCreateUser(
        id: String,
        name: String,
        completion: @escaping (User) -> Void
    ) {

        let recordID = CKRecord.ID(recordName: id)

        database.fetch(withRecordID: recordID) { record, error in

            if let record {

                completion(self.makeUser(from: record))
                return
            }

            let user = User(
                userID: id,
                displayName: name,
                joinedDate: .now,
                isFounder: true,
                watchedMovies: [],
                skippedMovies: [],
                settings: UserSettings()
            )

            let record = self.makeRecord(from: user)

            self.database.save(record) { _, error in

                if let error {
                    print("❌ CloudKit save failed:", error.localizedDescription)
                } else {
                    completion(user)
                    print("✅ New user created")
                }
            }
        }
    }
    // MARK: save
    func save(user: User) {

        let recordID = CKRecord.ID(recordName: user.userID)

        database.fetch(withRecordID: recordID) { record, error in

            guard let record else {
                print("❌ User record not found.")
                return
            }

            record["displayName"] = user.displayName
            record["joinedDate"] = user.joinedDate
            record["isFounder"] = user.isFounder
            record["showReleaseYears"] = user.settings.showReleaseYears

            if !user.watchedMovies.isEmpty {
                record["watchedMovies"] = user.watchedMovies
            }
            
            if !user.skippedMovies.isEmpty {
                record["skippedMovies"] = user.skippedMovies
            }

            self.database.save(record) { _, error in

                if let error {
                    print("❌ Failed to save user:", error.localizedDescription)
                } else {
                    print("✅ User updated")
                }
            }
        }
    }
    //MARK: makeUser
    private func makeUser(from record: CKRecord) -> User {

        return User(
            userID: record["userID"] as? String ?? "",
            displayName: record["displayName"] as? String ?? "",
            joinedDate: record["joinedDate"] as? Date ?? .now,
            isFounder: record["isFounder"] as? Bool ?? false,
            watchedMovies: record["watchedMovies"] as? [String] ?? [],
            skippedMovies: record["skippedMovies"] as? [String] ?? [],
            settings: UserSettings(
                showReleaseYears: record["showReleaseYears"] as? Bool ?? true
            )
        )
    }

    //MARK: makeRecord
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
        
        if !user.skippedMovies.isEmpty {
            record["skippedMovies"] = user.skippedMovies
        }

        return record
    }
}
