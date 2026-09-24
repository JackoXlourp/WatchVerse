//
//  CloudKitService.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-09.
//

import CloudKit
import Foundation

final class CloudKitService {

    private let database = CKContainer.default().privateCloudDatabase

    // MARK: fetchUser
    func fetchUser(
        id: String,
        completion: @escaping (Result<User, Error>) -> Void
    ) {

        let recordID = CKRecord.ID(recordName: id)

        database.fetch(withRecordID: recordID) { record, error in

            if let error {
                completion(.failure(error))
                return
            }

            guard let record else {

                let error = NSError(
                    domain: "CloudKitService",
                    code: 1,
                    userInfo: [
                        NSLocalizedDescriptionKey:
                            "CloudKit user record was not found."
                    ]
                )

                completion(.failure(error))
                return
            }

            let user = self.makeUser(from: record)
            completion(.success(user))
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
                }(),
                journeyPositions: {
                    guard let data = record["journeyPositions"] as? Data,
                          let positions = try? JSONDecoder().decode([String: String].self, from: data)
                    else {
                        return [:]
                    }
                    return positions
                }()
            ),
            shownBadgePopups: record["shownBadgePopups"] as? [String] ?? [],
        )
    }

    // MARK: Delete User
    func deleteUser(
        id: String
    ) async throws {

        let recordID = CKRecord.ID(recordName: id)

        try await withCheckedThrowingContinuation {
            (continuation: CheckedContinuation<Void, Error>) in

            database.delete(
                withRecordID: recordID
            ) { _, error in

                if let error = error as? CKError,
                   error.code == .unknownItem {

                    continuation.resume(
                        returning: ()
                    )

                    return
                }

                if let error {
                    continuation.resume(
                        throwing: error
                    )

                    return
                }

                continuation.resume(
                    returning: ()
                )
            }
        }
    }
}
