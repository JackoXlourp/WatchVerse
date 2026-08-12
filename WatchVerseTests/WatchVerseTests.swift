//
//  WatchVerseTests.swift
//  WatchVerseTests
//
//  Created by Maxime Proulx on 2026-07-26.
//

import Foundation
import Testing
@testable import WatchVerse

struct WatchVerseTests {

    @Test
    func infinitySagaBadgeUnlocksAfterAllRequiredMoviesAreWatched() {

        let requiredIDs = BadgeData.all
            .first(where: { $0.id == "infinity-saga" })?
            .requiredMovieIDs ?? []

        #expect(requiredIDs.isEmpty == false)

        let universe = Universe(
            id: "test-universe",
            title: "Test Universe",
            subtitle: "",
            fullTitle: "Test Universe",
            description: "",
            logo: "",
            banner: "",
            poster: "",
            movies: requiredIDs.map {
                Movie(
                    id: $0,
                    title: $0,
                    poster: "",
                    year: 2000,
                    runtime: "",
                    phase: "",
                    synopsis: "",
                    director: "",
                    genres: [],
                    isWatched: false,
                    isSkipped: false
                )
            }
        )

        let viewModel = JourneyViewModel(
            journey: universe,
            universes: [universe]
        )

        let authentication = AuthenticationService()

        authentication.currentUser = User(
            userID: "test-user",
            displayName: "Test",
            joinedDate: .now,
            isFounder: false,
            watchedMovies: [],
            skippedMovies: [],
            unlockedBadges: [],
            settings: UserSettings()
        )

        viewModel.authentication = authentication

        for id in requiredIDs {
            viewModel.markMovieWatched(id: id)
        }

        #expect(
            authentication.currentUser?.unlockedBadges.contains("infinity-saga") == true
        )
    }
}
