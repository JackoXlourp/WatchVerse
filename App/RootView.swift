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
    }
}
