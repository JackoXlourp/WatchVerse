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

    func createUser(id: String, name: String) {

        let record = CKRecord(recordType: "User")

        record["userID"] = id
        record["displayName"] = name
        record["joinedDate"] = Date()

        database.save(record) { _, error in

            if let error {
                print("❌ CloudKit save failed:", error.localizedDescription)
            } else {
                print("✅ User saved to CloudKit")
            }
        }
    }
}
