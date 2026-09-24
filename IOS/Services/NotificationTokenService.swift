//
//  NotificationTokenService.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-22.
//

import Foundation
import FirebaseAuth

enum NotificationTokenService {

    private static let tokenKey = "latestFCMToken"

    static func storeAndSync(_ token: String) {

        UserDefaults.standard.set(
            token,
            forKey: tokenKey
        )

        Task {
            await syncStoredTokenIfPossible()
        }
    }

    static func syncStoredTokenIfPossible() async {

        guard
            let token = UserDefaults.standard.string(
                forKey: tokenKey
            ),
            let uid = Auth.auth().currentUser?.uid
        else {
            return
        }

        do {
            try await FirestoreUserService()
                .addFCMToken(
                    uid: uid,
                    token: token
                )

            print("✅ FCM token saved to Firestore")

        } catch {

            print(
                "❌ FCM token save failed:",
                error.localizedDescription
            )
        }
    }
    
    static func removeStoredTokenFromCurrentUser() async {

        guard
            let token = UserDefaults.standard.string(
                forKey: tokenKey
            ),
            let uid = Auth.auth().currentUser?.uid
        else {
            return
        }

        do {
            try await FirestoreUserService()
                .removeFCMToken(
                    uid: uid,
                    token: token
                )

            print("✅ FCM token removed from Firestore")

        } catch {

            print(
                "❌ FCM token removal failed:",
                error.localizedDescription
            )
        }
    }
}
