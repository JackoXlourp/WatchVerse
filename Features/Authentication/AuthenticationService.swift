//
//  AuthenticationServicev.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-09.
//

import SwiftUI
import Foundation
import AuthenticationServices

@Observable
final class AuthenticationService {

    var isSignedIn = false

    @ObservationIgnored
    @AppStorage("appleUserID")
    private var storedUserID = ""
    
    var currentUser: User?
    var isLoading = true
    var needsName = false

    func configure(_ request: ASAuthorizationAppleIDRequest) {

        request.requestedScopes = [
            .fullName,
            .email
        ]
    }

    func handle(_ result: Result<ASAuthorization, Error>) {

        switch result {

        case .success(let authorization):

            guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential else {
                return
            }
            
            storedUserID = credential.user
            
            currentUser = User(
                userID: credential.user,
                displayName: credential.fullName?.givenName ?? "",
                joinedDate: .now,
                isFounder: true,
                watchedMovies: [],
                skippedMovies: [],
                unlockedBadges: [],
                settings: UserSettings(),
                shownBadgePopups: []
            )
            
            needsName = false

            isSignedIn = true
            

        case .failure(let error):

            print("Apple Sign In failed: \(error.localizedDescription)")
        }
    }
    
    func restoreSession() {
        
        isLoading = true

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
                        name: self.currentUser?.displayName ?? ""
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
}
