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
    
    @Environment(AppNavigation.self)
    private var navigation

    @State private var showingBadgeOverlay = false
    @State private var popupBadge: Badge?

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

                if user.isFounder,
                   !user.shownBadgePopups.contains("founder"),
                   let badge = BadgeData.all.first(where: { $0.id == "founder" }) {

                    viewModel.pendingBadgePopup = badge
                }
            }
        }
        .onChange(of: viewModel.pendingBadgePopup) { _, badge in

            guard let badge else {
                return
            }

            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {

                popupBadge = badge
                showingBadgeOverlay = true

                if var user = authentication.currentUser {

                    if !user.shownBadgePopups.contains(badge.id) {
                        user.shownBadgePopups.append(badge.id)

                        authentication.currentUser = user
                        cloudKit.save(user: user)
                    }
                }

                viewModel.pendingBadgePopup = nil
            }
        }
        .overlay {
            if showingBadgeOverlay,
               let badge = popupBadge {

                BadgeUnlockOverlay(
                    badge: badge,
                    onClose: {
                        showingBadgeOverlay = false
                    },
                    onSeeBadge: {
                        showingBadgeOverlay = false
                        navigation.selectedTab = .badges
                    }
                )
            }
        }
    }
}

