//
//  AppNavigation.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-08.
//

import SwiftUI

@Observable
class AppNavigation {

    var selectedTab: Tab = .home
    var pendingContent: ContentDestination?
    var pendingBadgeID: String?
    private(set) var isActivatingUniverse = false

    struct ContentDestination: Equatable {
        let universeID: String
        let contentID: String
    }

    @MainActor
    func openUniverse(
        _ universe: Universe,
        contentID: String? = nil,
        contentStore: ContentStore,
        authentication: AuthenticationService,
        viewModel: JourneyViewModel
    ) async {
        guard !isActivatingUniverse,
              let userID = authentication.currentUser?.userID else {
            return
        }

        isActivatingUniverse = true
        defer { isActivatingUniverse = false }

        await contentStore.preloadArtwork(for: universe)

        guard var user = authentication.currentUser,
              user.userID == userID else {
            return
        }

        user.settings.currentUniverseID = universe.id
        authentication.currentUser = user
        await authentication.saveCurrentUserToFirestore()

        guard authentication.currentUser?.userID == userID else {
            return
        }

        viewModel.replaceJourney(with: universe)
        pendingContent = contentID.map {
            ContentDestination(universeID: universe.id, contentID: $0)
        }
        selectedTab = .journey
    }

    func openBadge(id: String) {
        pendingBadgeID = id
        selectedTab = .badges
    }

    enum Tab: Hashable {
        case home
        case journey
        case search
        case badges
    }
}
