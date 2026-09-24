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
import CloudKit

@Observable
final class AuthenticationService {
    
    private var currentNonce: String?
    private var appleReauthenticationController: ASAuthorizationController?
    private var appleReauthenticationDelegate: AppleReauthenticationDelegate?

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

                currentNonce = nil
                print("Could not create Firebase credential.")
                return
            }

            signInToFirebase(with: firebaseCredential) { result in

                switch result {

                case .success(let authResult):

                    DispatchQueue.main.async {

                        self.existingFirestoreProfile = nil
                        UserDefaults.standard.removeObject(
                            forKey: "notifyNewUniverses"
                        )
                        self.firebaseUID = authResult.user.uid
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

            currentNonce = nil
            print("Apple Sign In failed: \(error.localizedDescription)")
        }
    }
    
    func restoreSession() {
        
        isLoading = true
        existingFirestoreProfile = nil
        currentUser = nil
        needsName = false
        isSignedIn = false
        UserDefaults.standard.removeObject(
            forKey: "notifyNewUniverses"
        )
        firebaseUID = Auth.auth().currentUser?.uid
        
        guard firebaseUID != nil else {
            isSignedIn = false
            isLoading = false
            return
        }

        guard !storedUserID.isEmpty else {

            guard let firebaseUID else {
                isLoading = false
                return
            }

            Task {

                do {

                    let loadedFromFirestore =
                        try await self.loadFirestoreUser(
                            uid: firebaseUID
                        )

                    await MainActor.run {

                        if !loadedFromFirestore {
                            self.currentUser = User(
                                userID: firebaseUID,
                                displayName:
                                    Auth.auth().currentUser?.displayName
                                    ?? "",
                                joinedDate: .now,
                                isFounder: false,
                                watchedMovies: [],
                                skippedMovies: [],
                                unlockedBadges: [],
                                settings: UserSettings(),
                                shownBadgePopups: []
                            )
                            self.needsName = true
                        }

                        self.isSignedIn = true
                        self.isLoading = false
                    }

                } catch {

                    print(
                        "Firestore user load failed:",
                        error.localizedDescription
                    )

                    await MainActor.run {
                        self.isLoading = false
                    }
                }
            }

            return
        }

        let provider = ASAuthorizationAppleIDProvider()

        provider.getCredentialState(forUserID: storedUserID) { state, _ in

            DispatchQueue.main.async {

                switch state {

                case .authorized:

                    self.needsName = false

                    guard let firebaseUID = self.firebaseUID else {
                        self.isLoading = false
                        return
                    }

                    Task {

                        do {

                            let loadedFromFirestore =
                                try await self.loadFirestoreUser(
                                    uid: firebaseUID
                                )

                            if loadedFromFirestore {

                                await MainActor.run {
                                    self.isSignedIn = true
                                    self.isLoading = false
                                }

                                return
                            }

                            let legacyUserExists =
                                try await self.loadLegacyCloudKitUserIfPresent()

                            await MainActor.run {

                                if !legacyUserExists {

                                    self.currentUser = User(
                                        userID: self.storedUserID,
                                        displayName: "",
                                        joinedDate: .now,
                                        isFounder: false,
                                        watchedMovies: [],
                                        skippedMovies: [],
                                        unlockedBadges: [],
                                        settings: UserSettings(),
                                        shownBadgePopups: []
                                    )

                                    self.needsName = true
                                }

                                self.isSignedIn = true
                                self.isLoading = false
                            }

                        } catch {

                            print(
                                "Firestore user load failed:",
                                error.localizedDescription
                            )

                            await MainActor.run {
                                self.isLoading = false
                            }
                        }
                    }
                    
                    
                default:
                    try? Auth.auth().signOut()
                    self.firebaseUID = nil
                    self.existingFirestoreProfile = nil
                    self.currentUser = nil
                    UserDefaults.standard.removeObject(
                        forKey: "notifyNewUniverses"
                    )
                    self.isSignedIn = false
                    self.isLoading = false
                }
            }
        }
    }
    func logout() {

        Task {

            await NotificationTokenService
                .removeStoredTokenFromCurrentUser()

            await MainActor.run {

                do {
                    try Auth.auth().signOut()
                } catch {
                    print(
                        "Firebase sign out failed:",
                        error.localizedDescription
                    )
                }

                self.firebaseUID = nil
                self.existingFirestoreProfile = nil
                self.storedUserID = ""

                UserDefaults.standard.removeObject(
                    forKey: "notifyNewUniverses"
                )

                self.currentUser = nil
                self.isSignedIn = false
                self.needsName = false
            }
        }
    }
    func deleteAccount() async -> Bool {

        guard let firebaseUser = Auth.auth().currentUser else {
            return false
        }

        let uid = firebaseUser.uid

        do {

            try await reauthenticateCurrentUserWithApple(
                firebaseUser
            )

            let profileToRestore = try await FirestoreUserService()
                .fetchProfile(uid: uid)

            try await FirestoreUserService()
                .deleteProfile(uid: uid)

            do {
                try await firebaseUser.delete()
            } catch {

                if let profileToRestore {
                    try? await FirestoreUserService()
                        .saveProfile(profileToRestore)
                }

                throw error
            }

            await MainActor.run {
                self.firebaseUID = nil
                self.existingFirestoreProfile = nil
                self.storedUserID = ""
                UserDefaults.standard.removeObject(
                    forKey: "notifyNewUniverses"
                )
                self.currentUser = nil
                self.isSignedIn = false
                self.needsName = false
            }

            return true

        } catch {

            print(
                "❌ Account deletion failed:",
                error.localizedDescription
            )

            return false
        }
    }
    
    func migrateLegacyUserIfNeeded(
        notifyNewUniverses: Bool
    ) async {

        guard let firebaseUID else {
            return
        }

        do {

            let legacyUser =
                try await fetchLegacyCloudKitUser()

            if let existingProfile =
                try await FirestoreUserService()
                    .fetchProfile(uid: firebaseUID) {

                await MainActor.run {
                    self.existingFirestoreProfile =
                        existingProfile
                }
                
                await NotificationTokenService
                    .syncStoredTokenIfPossible()

                try await CloudKitService()
                    .deleteUser(
                        id: legacyUser.userID
                    )

                return
            }

            let migrationProfile =
                FirestoreUserProfile.migrated(
                    from: legacyUser,
                    firebaseUID: firebaseUID,
                    email:
                        Auth.auth().currentUser?.email
                        ?? "",
                    notifyNewUniverses:
                        notifyNewUniverses
                )

            try await CloudKitMigrationService()
                .migrate(
                    profile: migrationProfile
                )

            guard let migratedProfile =
                try await FirestoreUserService()
                    .fetchProfile(
                        uid: firebaseUID
                    )
            else {

                throw NSError(
                    domain: "AuthenticationService",
                    code: 3,
                    userInfo: [
                        NSLocalizedDescriptionKey:
                            "Firestore migration could not be verified."
                    ]
                )
            }

            await MainActor.run {
                self.existingFirestoreProfile =
                    migratedProfile
            }
            
            await NotificationTokenService
                .syncStoredTokenIfPossible()

            try await CloudKitService()
                .deleteUser(
                    id: legacyUser.userID
                )

        } catch {

            print(
                "❌ CloudKit migration failed:",
                error.localizedDescription
            )
        }
    }

    func saveCurrentUserToFirestore() async {

        let notifyNewUniverses =
            UserDefaults.standard.bool(
                forKey: "notifyNewUniverses"
            )

        await saveCurrentUserToFirestore(
            notifyNewUniverses: notifyNewUniverses
        )
    }
    
    func saveCurrentUserToFirestore(
        notifyNewUniverses: Bool
    ) async {

        guard
            let firebaseUID,
            let user = currentUser,
            Auth.auth().currentUser?.uid == firebaseUID
        else {
            return
        }

        do {

            let existingProfile: FirestoreUserProfile?

            if let cachedProfile = existingFirestoreProfile {
                existingProfile =
                    cachedProfile.uid == firebaseUID
                    ? cachedProfile
                    : try await FirestoreUserService()
                        .fetchProfile(uid: firebaseUID)
            } else {
                existingProfile = try await FirestoreUserService()
                    .fetchProfile(uid: firebaseUID)
            }

            guard var profile = existingProfile else {
                return
            }

            guard profile.uid == firebaseUID else {
                return
            }

            guard Auth.auth().currentUser?.uid == firebaseUID else {
                return
            }

            profile.displayName = user.displayName
            profile.isFounder = user.isFounder
            profile.showReleaseYears = user.settings.showReleaseYears
            profile.notifyNewUniverses = notifyNewUniverses
            profile.currentUniverseID = user.settings.currentUniverseID

            profile.selectedUniverseFilters =
                user.settings.selectedUniverseFilters.mapValues {
                    Array($0)
                }

            profile.journeyPositions =
                user.settings.journeyPositions

            profile.watchedMovies =
                user.watchedMovies

            profile.skippedMovies =
                user.skippedMovies

            profile.unlockedBadges =
                user.unlockedBadges

            profile.shownBadgePopups =
                user.shownBadgePopups

            try await FirestoreUserService()
                .updateMutableProfile(profile)

            await MainActor.run {
                self.existingFirestoreProfile = profile
            }

        } catch {

            print(
                "❌ Firestore user save failed:",
                error.localizedDescription
            )
        }
    }
    
    func createFirestoreProfileForNewUser(
        notifyNewUniverses: Bool
    ) async {

        guard
            let firebaseUID,
            let user = currentUser
        else {
            return
        }

        let profile = FirestoreUserProfile(
            uid: firebaseUID,
            displayName: user.displayName,
            email: Auth.auth().currentUser?.email ?? "",
            joinedDate: Int64(
                user.joinedDate.timeIntervalSince1970 * 1000
            ),
            isFounder: false,
            showReleaseYears: user.settings.showReleaseYears,
            notifyNewUniverses: notifyNewUniverses,
            currentUniverseID: user.settings.currentUniverseID,
            selectedUniverseFilters:
                user.settings.selectedUniverseFilters.mapValues {
                    Array($0)
                },
            journeyPositions:
                user.settings.journeyPositions,
            watchedMovies:
                user.watchedMovies,
            skippedMovies:
                user.skippedMovies,
            unlockedBadges:
                user.unlockedBadges,
            shownBadgePopups:
                user.shownBadgePopups,
            schemaVersion: 2,
            cloudKitMigrationVersion: nil,
            legacyCloudKitRecordID: nil
        )

        do {

            try await FirestoreUserService()
                .saveProfile(profile)

            await MainActor.run {
                self.existingFirestoreProfile = profile
            }
            
            await NotificationTokenService
                .syncStoredTokenIfPossible()

        } catch {

            print(
                "❌ Firestore profile creation failed:",
                error.localizedDescription
            )
        }
    }
    
    func loadFirestoreUser(
        uid: String
    ) async throws -> Bool {

        guard let profile =
            try await FirestoreUserService()
                .fetchProfile(uid: uid)
        else {
            return false
        }

        await MainActor.run {
            self.existingFirestoreProfile = profile
            self.currentUser = profile.user
            self.needsName = false
            UserDefaults.standard.set(
                profile.notifyNewUniverses,
                forKey: "notifyNewUniverses"
            )
        }
        
        await NotificationTokenService
            .syncStoredTokenIfPossible()

        return true
    }
    
    func loadLegacyCloudKitUserIfPresent() async throws -> Bool {

        guard !storedUserID.isEmpty else {
            return false
        }

        do {

            let legacyUser =
                try await fetchLegacyCloudKitUser()

            await MainActor.run {
                self.currentUser = legacyUser
                self.needsName = false
            }

            return true

        } catch let error as CKError
            where error.code == .unknownItem {

            return false
        }
    }
    
    private func fetchLegacyCloudKitUser() async throws -> User {

        guard !storedUserID.isEmpty else {
            throw NSError(
                domain: "AuthenticationService",
                code: 2,
                userInfo: [
                    NSLocalizedDescriptionKey:
                        "No legacy Apple user ID is available."
                ]
            )
        }

        return try await withCheckedThrowingContinuation { continuation in

            CloudKitService().fetchUser(
                id: storedUserID
            ) { result in

                continuation.resume(with: result)
            }
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
    private func reauthenticateCurrentUserWithApple(
        _ firebaseUser: FirebaseAuth.User
    ) async throws {

        defer {
            currentNonce = nil
        }

        let appleCredential =
            try await requestAppleReauthenticationCredential()

        guard let credential =
            firebaseCredential(from: appleCredential) else {

            throw NSError(
                domain: "AuthenticationService",
                code: 4,
                userInfo: [
                    NSLocalizedDescriptionKey:
                        "Could not create a Firebase credential for Apple reauthentication."
                ]
            )
        }

        _ = try await firebaseUser.reauthenticate(
            with: credential
        )
    }

    private func requestAppleReauthenticationCredential() async throws
        -> ASAuthorizationAppleIDCredential {

        try await withCheckedThrowingContinuation {
            (continuation: CheckedContinuation<ASAuthorizationAppleIDCredential, Error>) in

            let request = ASAuthorizationAppleIDProvider()
                .createRequest()

            configure(request)

            let delegate = AppleReauthenticationDelegate { result in

                self.appleReauthenticationController = nil
                self.appleReauthenticationDelegate = nil
                continuation.resume(with: result)
            }

            let controller = ASAuthorizationController(
                authorizationRequests: [request]
            )

            appleReauthenticationDelegate = delegate
            appleReauthenticationController = controller
            controller.delegate = delegate
            controller.presentationContextProvider = delegate
            controller.performRequests()
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

private final class AppleReauthenticationDelegate: NSObject,
    ASAuthorizationControllerDelegate,
    ASAuthorizationControllerPresentationContextProviding {

    private let completion:
        (Result<ASAuthorizationAppleIDCredential, Error>) -> Void

    init(
        completion: @escaping (Result<ASAuthorizationAppleIDCredential, Error>) -> Void
    ) {
        self.completion = completion
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {

        guard let credential =
            authorization.credential as? ASAuthorizationAppleIDCredential else {

            completion(
                .failure(
                    NSError(
                        domain: "AuthenticationService",
                        code: 5,
                        userInfo: [
                            NSLocalizedDescriptionKey:
                                "Apple reauthentication returned an unexpected credential."
                        ]
                    )
                )
            )

            return
        }

        completion(.success(credential))
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {

        completion(.failure(error))
    }

    func presentationAnchor(
        for controller: ASAuthorizationController
    ) -> ASPresentationAnchor {

        let activeWindowScene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }

        guard let windowScene = activeWindowScene
        else {
            fatalError("Apple reauthentication requires an active window scene.")
        }

        return windowScene.windows.first(where: \.isKeyWindow)
            ?? UIWindow(windowScene: windowScene)
    }
}
