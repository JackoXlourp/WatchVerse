//
//  RootView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct RootView: View {
    
    @Environment(JourneyViewModel.self)
    private var viewModel

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
                  let user = authentication.currentUser else {
                return
            }

            cloudKit.findOrCreateUser(
                id: user.userID,
                name: user.displayName
            ) { user in

                authentication.currentUser = user
                viewModel.loadWatchedMovies(from: user)
            }
        }
    }
}
