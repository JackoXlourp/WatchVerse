//
//  JourneyViewModel.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-29.
//

import SwiftUI

@Observable
class JourneyViewModel {
    
    private let watchedMoviesKey = "watchedMovies"
    private let skippedMoviesKey = "skippedMovies"
    
    let journey: Universe
    let universes: [Universe]
    
    var movies: [Movie]
    
    init(journey: Universe, universes: [Universe]) {
        self.journey = journey
        self.universes = universes
        self.movies = journey.movies
        loadWatchedMovies()
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
    
    func markMovieWatched(id: String) {
        guard let index = movies.firstIndex(where: { $0.id == id }) else {return}
        
        movies[index].isWatched = true
        movies[index].isSkipped = false
        saveWatchedMovies()
    }
    
    func markMovieUnwatched(id: String) {
        guard let index = movies.firstIndex(where: { $0.id == id }) else { return }

        movies[index].isWatched = false
        saveWatchedMovies()
    }
    
    func skipMovie(id: String) {
        guard let index = movies.firstIndex(where: { $0.id == id }) else { return }

        movies[index].isSkipped = true
        saveWatchedMovies()
    }

    func unskipMovie(id: String) {
        guard let index = movies.firstIndex(where: { $0.id == id }) else { return }

        movies[index].isSkipped = false
        saveWatchedMovies()
    }
    
    func nextCurrentMovieIndex() -> Int? {
        currentMovieIndex
    }
    
    func resetJourney() {
        for index in movies.indices {
            movies[index].isWatched = false
            movies[index].isSkipped = false
        }
        saveWatchedMovies()
    }
    
    private func saveWatchedMovies() {
        let watchedIDs = movies
            .filter(\.isWatched)
            .map(\.id)

        let skippedIDs = movies
            .filter(\.isSkipped)
            .map(\.id)

        UserDefaults.standard.set(watchedIDs, forKey: watchedMoviesKey)
        UserDefaults.standard.set(skippedIDs, forKey: skippedMoviesKey)
    }
    
    private func loadWatchedMovies() {
        let watchedIDs = UserDefaults.standard.stringArray(forKey: watchedMoviesKey) ?? []
        let skippedIDs = UserDefaults.standard.stringArray(forKey: skippedMoviesKey) ?? []

        for index in movies.indices {
            movies[index].isWatched = watchedIDs.contains(movies[index].id)
            movies[index].isSkipped = skippedIDs.contains(movies[index].id)
        }
    }
    
}

