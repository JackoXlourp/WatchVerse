//
//  AuthenticationService.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-09.
//

import SwiftUI
import Foundation
import AuthenticationServices
import FirebaseAuth
import CryptoKit

@Observable
final class AuthenticationService {
    
    private var currentNonce: String?

    var isSignedIn = false

    @ObservationIgnored
    @AppStorage("appleUserID")
    private var storedUserID = ""
    
    var currentUser: User?
    var firebaseUID: String?
    var existingFirestoreProfile: FirestoreUserProfile?
    var isLoading = true
    var needsName = false

    func configure(_ request: ASAuthorizationAppleIDRequest) {

        let nonce = randomNonceString()
        currentNonce = nonce

        request.requestedScopes = [
            .fullName,
            .email
        ]

        request.nonce = sha256(nonce)
    }

    func handle(_ result: Result<ASAuthorization, Error>) {

        switch result {

        case .success(let authorization):

            guard let appleCredential =
                authorization.credential as? ASAuthorizationAppleIDCredential else {
                return
            }

            guard let firebaseCredential =
                firebaseCredential(from: appleCredential) else {

                print("Could not create Firebase credential.")
                return
            }

            signInToFirebase(with: firebaseCredential) { result in

                switch result {

                case .success(let authResult):

                    DispatchQueue.main.async {

                        self.firebaseUID = authResult.user.uid
                        Task {
                            await self.loadExistingFirestoreProfile(
                                uid: authResult.user.uid
                            )
                        }
                        self.currentNonce = nil
                        self.storedUserID = appleCredential.user

                        self.currentUser = User(
                            userID: appleCredential.user,
                            displayName: appleCredential.fullName?.givenName ?? "",
                            joinedDate: .now,
                            isFounder: false,
                            watchedMovies: [],
                            skippedMovies: [],
                            unlockedBadges: [],
                            settings: UserSettings(),
                            shownBadgePopups: []
                        )

                        self.needsName = false
                        self.isSignedIn = true
                    }

                case .failure(let error):

                    DispatchQueue.main.async {
                        self.currentNonce = nil

                        print(
                            "Firebase Sign In failed: \(error.localizedDescription)"
                        )
                    }
                }
            }
            

        case .failure(let error):

            print("Apple Sign In failed: \(error.localizedDescription)")
        }
    }
    
    func restoreSession() {
        
        isLoading = true
        firebaseUID = Auth.auth().currentUser?.uid

        guard !storedUserID.isEmpty else {
            isLoading = false
            return
        }

        let provider = ASAuthorizationAppleIDProvider()

        provider.getCredentialState(forUserID: storedUserID) { state, _ in

            DispatchQueue.main.async {

                switch state {

                case .authorized:
                    
                    self.needsName = false

                    CloudKitService().findOrCreateUser(
                        id: self.storedUserID,
                        name: self.currentUser?.displayName ?? "",
                        onFailure: { _ in
                                self.isLoading = false
                            }
                    ) { user, isNewUser in
                        self.currentUser = user
                        self.needsName = isNewUser
                        self.isSignedIn = true
                        self.isLoading = false
                    }
                    
                    
                default:
                    self.isSignedIn = false
                    self.isLoading = false
                }
            }
        }
    }
    func logout() {

        do {
            try Auth.auth().signOut()
        } catch {
            print("Firebase sign out failed: \(error.localizedDescription)")
        }

        firebaseUID = nil
        storedUserID = ""
        currentUser = nil
        isSignedIn = false
        needsName = false
    }
    func deleteAccount(cloudKit: CloudKitService) {

        guard let userID = currentUser?.userID else {
            return
        }

        cloudKit.deleteUser(id: userID) {

            self.storedUserID = ""
            self.currentUser = nil
            self.isSignedIn = false
            self.needsName = false

        }
    }
    
    func migrateLegacyUserIfNeeded(
        notifyNewUniverses: Bool
    ) async {

        guard
            let firebaseUID,
            let legacyUser = currentUser
        else {
            return
        }

        do {

            if let existingProfile =
                try await FirestoreUserService()
                    .fetchProfile(uid: firebaseUID) {

                await MainActor.run {
                    self.existingFirestoreProfile = existingProfile
                }

                return
            }

            let migrationProfile =
                FirestoreUserProfile.migrated(
                    from: legacyUser,
                    firebaseUID: firebaseUID,
                    email: Auth.auth().currentUser?.email ?? "",
                    notifyNewUniverses: notifyNewUniverses
                )

            try await CloudKitMigrationService()
                .migrate(profile: migrationProfile)

            let migratedProfile =
                try await FirestoreUserService()
                    .fetchProfile(uid: firebaseUID)

            await MainActor.run {
                self.existingFirestoreProfile = migratedProfile
            }

            print("✅ CloudKit user migrated to Firestore")

        } catch {

            print(
                "❌ CloudKit migration failed:",
                error.localizedDescription
            )
        }
    }
    
    private func sha256(_ input: String) -> String {

        let inputData = Data(input.utf8)
        let hashedData = SHA256.hash(data: inputData)

        return hashedData.map {
            String(format: "%02x", $0)
        }
        .joined()
    }

    private func randomNonceString(length: Int = 32) -> String {

        precondition(length > 0)

        let charset: [Character] =
            Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")

        var result = ""
        var remainingLength = length

        while remainingLength > 0 {

            let randoms: [UInt8] = (0..<16).map { _ in
                var random: UInt8 = 0
                let errorCode = SecRandomCopyBytes(
                    kSecRandomDefault,
                    1,
                    &random
                )

                if errorCode != errSecSuccess {
                    fatalError(
                        "Unable to generate nonce. SecRandomCopyBytes failed with OSStatus \(errorCode)"
                    )
                }

                return random
            }

            randoms.forEach { random in

                if remainingLength == 0 {
                    return
                }

                if random < charset.count {
                    result.append(
                        charset[Int(random)]
                    )

                    remainingLength -= 1
                }
            }
        }

        return result
    }
    private func firebaseCredential(
        from appleCredential: ASAuthorizationAppleIDCredential
    ) -> AuthCredential? {

        guard
            let nonce = currentNonce,
            let identityToken = appleCredential.identityToken,
            let tokenString = String(
                data: identityToken,
                encoding: .utf8
            )
        else {
            return nil
        }

        return OAuthProvider.appleCredential(
            withIDToken: tokenString,
            rawNonce: nonce,
            fullName: appleCredential.fullName
        )
    }
    private func loadExistingFirestoreProfile(
        uid: String
    ) async {

        do {
            existingFirestoreProfile =
                try await FirestoreUserService()
                    .fetchProfile(uid: uid)

            print(
                "Existing Firestore profile:",
                existingFirestoreProfile != nil
            )

        } catch {
            print(
                "Firestore profile check failed:",
                error.localizedDescription
            )
        }
    }
    private func signInToFirebase(
        with credential: AuthCredential,
        completion: @escaping (Result<AuthDataResult, Error>) -> Void
    ) {

        Auth.auth().signIn(with: credential) { result, error in

            if let error {
                completion(.failure(error))
                return
            }

            guard let result else {
                completion(
                    .failure(
                        NSError(
                            domain: "AuthenticationService",
                            code: 1,
                            userInfo: [
                                NSLocalizedDescriptionKey:
                                    "Firebase sign-in returned no user."
                            ]
                        )
                    )
                )
                return
            }

            completion(.success(result))
        }
    }
}
