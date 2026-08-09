//
//  RootView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct RootView: View {

    @Environment(AuthenticationService.self)
    private var authentication
    
    @Environment(CloudKitService.self)
    private var cloudKit

    var body: some View {

        Group {

            if authentication.isSignedIn {

                MainTabView()

            } else {

                AuthenticationView()
            }

        }
        .task {
            authentication.restoreSession()
        }
        .onChange(of: authentication.isSignedIn) { _, signedIn in

            guard signedIn,
                  let userID = authentication.userID else {
                return
            }

            cloudKit.findOrCreateUser(
                id: userID,
                name: authentication.displayName
            ) { user in

                print("Loaded user: \(user.displayName)")

            }
        }
    }
}
