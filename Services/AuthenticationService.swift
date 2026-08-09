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

    var userID: String?
    var displayName: String?

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

            userID = credential.user
            storedUserID = credential.user

            if let givenName = credential.fullName?.givenName {

                displayName = givenName

            } else {

                displayName = "Watcher"
            }

            isSignedIn = true

        case .failure(let error):

            print("Apple Sign In failed: \(error.localizedDescription)")
        }
    }
    
    func restoreSession() {

        guard !storedUserID.isEmpty else {
            return
        }

        let provider = ASAuthorizationAppleIDProvider()

        provider.getCredentialState(forUserID: storedUserID) { state, _ in

            DispatchQueue.main.async {

                switch state {

                case .authorized:
                    self.userID = self.storedUserID
                    self.isSignedIn = true

                default:
                    self.isSignedIn = false
                }
            }
        }
    }
}
