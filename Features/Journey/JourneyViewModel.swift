//
//  JourneyViewModel.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-29.
//

import SwiftUI

@Observable
class JourneyViewModel {
    
    @ObservationIgnored
    var authentication: AuthenticationService?

    @ObservationIgnored
    var cloudKit: CloudKitService?
    
    let journey: Universe
    let universes: [Universe]
    
    var movies: [Movie]
    
    init(journey: Universe, universes: [Universe]) {
        self.journey = journey
        self.universes = universes
        self.movies = journey.movies
    }
    
    var movieCount: Int {
        movies.count
    }
    
    var nextMovie: Movie? {
        movies.first(where: { !$0.isWatched})
    }
    
    func movie(at index: Int) -> Movie {
        movies[index]
    }
    
    var watchedCount: Int {
        movies.filter { $0.isWatched || $0.isSkipped }.count
    }
    
    var isJourneyComplete: Bool {
        watchedCount == movieCount
    }
    
    var currentMovie: Movie? {
        movies.first(where: { !$0.isWatched })
    }
    
    var currentMovieIndex: Int? {
        movies.firstIndex(where: { !$0.isWatched && !$0.isSkipped })
    }
    
    var pendingBadgePopup: Badge?
    
    //MARK: func
    func markMovieWatched(id: String) {

        guard let index = movies.firstIndex(where: { $0.id == id }) else {
            return
        }

        movies[index].isWatched = true
        movies[index].isSkipped = false

        if var user = authentication?.currentUser {

            if !user.watchedMovies.contains(id) {
                user.watchedMovies.append(id)
            }

            user.skippedMovies.removeAll { $0 == id }

            authentication?.currentUser = user
            cloudKit?.save(user: user)

            checkForBadgeUnlocks()
        }
    }
    
    private func checkForBadgeUnlocks() {

        guard var user = authentication?.currentUser else {
            return
        }

        var didUnlockBadge = false

        for badge in BadgeData.all {

            guard !badge.requiredMovieIDs.isEmpty else {
                continue
            }

            guard !user.unlockedBadges.contains(badge.id) else {
                continue
            }

            let hasCompletedRequirements = badge.requiredMovieIDs.allSatisfy {
                user.watchedMovies.contains($0)
            }

            if hasCompletedRequirements {
                user.unlockedBadges.append(badge.id)
                pendingBadgePopup = badge
                didUnlockBadge = true
            }
        }

        if didUnlockBadge {
            authentication?.currentUser = user
            cloudKit?.save(user: user)
        }
    }
    
    func markMovieUnwatched(id: String) {

        guard let index = movies.firstIndex(where: { $0.id == id }) else {
            return
        }

        movies[index].isWatched = false

        if var user = authentication?.currentUser {

            user.watchedMovies.removeAll { $0 == id }

            authentication?.currentUser = user
            cloudKit?.save(user: user)
        }
    }
    
    func skipMovie(id: String) {

        guard let index = movies.firstIndex(where: { $0.id == id }) else {
            return
        }

        movies[index].isSkipped = true
        movies[index].isWatched = false

        if var user = authentication?.currentUser {

            if !user.skippedMovies.contains(id) {
                user.skippedMovies.append(id)
            }

            user.watchedMovies.removeAll { $0 == id }

            authentication?.currentUser = user
            cloudKit?.save(user: user)
        }
    }

    func unskipMovie(id: String) {

        guard let index = movies.firstIndex(where: { $0.id == id }) else {
            return
        }

        movies[index].isSkipped = false

        if var user = authentication?.currentUser {

            user.skippedMovies.removeAll { $0 == id }

            authentication?.currentUser = user
            cloudKit?.save(user: user)
        }
    }
    
    func nextCurrentMovieIndex() -> Int? {
        currentMovieIndex
    }
    
    func resetJourney() {

        for index in movies.indices {
            movies[index].isWatched = false
            movies[index].isSkipped = false
        }

        if var user = authentication?.currentUser {

            user.watchedMovies.removeAll()
            user.skippedMovies.removeAll()

            authentication?.currentUser = user
            cloudKit?.save(user: user)
        }
    }
    
    func loadWatchedMovies(from user: User) {

        for index in movies.indices {
            movies[index].isWatched = user.watchedMovies.contains(movies[index].id)
            movies[index].isSkipped = user.skippedMovies.contains(movies[index].id)
            
        }
    }
    
}
