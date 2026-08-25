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
        completion: @escaping (User, Bool) -> Void
    ) {

        let recordID = CKRecord.ID(recordName: id)

        database.fetch(withRecordID: recordID) { record, error in

            if let record {
                let user = self.makeUser(from: record)
                completion(user, false)
                return
            }

            if let error = error as? CKError {

                if error.code != .unknownItem {

                    print("❌ CloudKit fetch failed:", error.localizedDescription)
                    return
                }
            }

            let user = User(
                userID: id,
                displayName: name,
                joinedDate: .now,
                isFounder: false,
                watchedMovies: [],
                skippedMovies: [],
                unlockedBadges: [],
                settings: UserSettings(),
                shownBadgePopups: []
            )

            let record = self.makeRecord(from: user)

            self.database.save(record) { _, error in

                if let error {
                    print("❌ Failed to create user:", error.localizedDescription)
                    return
                }

                completion(user, true)
                print("✅ New user created")
            }
        }
    }
    // MARK: save
    func save(user: User) {

        let recordID = CKRecord.ID(recordName: user.userID)

        database.fetch(withRecordID: recordID) { record, error in

            if let error {
                print("❌ CloudKit save failed:", error.localizedDescription)
                return
            }

            guard let record else {
                print("❌ CloudKit save failed: User record not found")
                return
            }

            record["displayName"] = user.displayName
            record["joinedDate"] = user.joinedDate
            record["isFounder"] = user.isFounder
            record["showReleaseYears"] = user.settings.showReleaseYears
            if let data = try? JSONEncoder().encode(user.settings.selectedUniverseFilters) {
                record["selectedUniverseFilters"] = data
            }

            record["watchedMovies"] = user.watchedMovies
            record["skippedMovies"] = user.skippedMovies
            record["unlockedBadges"] = user.unlockedBadges
            record["shownBadgePopups"] = user.shownBadgePopups

            self.database.save(record) { _, error in

                if let error {
                    print("❌ CloudKit save failed:", error.localizedDescription)
                    return
                }
                print("✅ CloudKit save success")
            }
        }
    }
    
    //MARK: makeUser
    private func makeUser(from record: CKRecord) -> User {

        return User(
            userID: record.recordID.recordName,
            displayName: record["displayName"] as? String ?? "",
            joinedDate: record["joinedDate"] as? Date ?? .now,
            isFounder: record["isFounder"] as? Bool ?? false,
            watchedMovies: record["watchedMovies"] as? [String] ?? [],
            skippedMovies: record["skippedMovies"] as? [String] ?? [],
            unlockedBadges: record["unlockedBadges"] as? [String] ?? [],
            settings: UserSettings(
                showReleaseYears: record["showReleaseYears"] as? Bool ?? true,
                selectedUniverseFilters: {
                    guard let data = record["selectedUniverseFilters"] as? Data,
                          let filters = try? JSONDecoder().decode([String: Set<String>].self, from: data)
                    else {
                        return [:]
                    }
                    return filters
                }()            ),
            shownBadgePopups: record["shownBadgePopups"] as? [String] ?? [],
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
        if let data = try? JSONEncoder().encode(user.settings.selectedUniverseFilters) {
            record["selectedUniverseFilters"] = data
        }

        record["watchedMovies"] = user.watchedMovies
        record["skippedMovies"] = user.skippedMovies
        record["unlockedBadges"] = user.unlockedBadges
        record["shownBadgePopups"] = user.shownBadgePopups

        return record
    }
    
    // MARK: Delete User

    func deleteUser(id: String, completion: @escaping () -> Void) {

        let recordID = CKRecord.ID(recordName: id)

        database.delete(withRecordID: recordID) { _, error in

            if let error {
                print("❌ CloudKit delete failed:", error.localizedDescription)
                return
            }

            print("🗑️ CloudKit user deleted:", id)

            DispatchQueue.main.async {
                completion()
            }
        }
    }
}
