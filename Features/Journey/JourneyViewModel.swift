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
        movies.filter(\.isWatched).count
    }
    
    var isJourneyComplete: Bool {
        watchedCount == movieCount
    }
    
    var currentMovie: Movie? {
        movies.first(where: { !$0.isWatched })
    }
    
    var currentMovieIndex: Int? {
        movies.firstIndex(where: { !$0.isWatched})
    }
    
    func markMovieWatched(id: String) {
        guard let index = movies.firstIndex(where: { $0.id == id }) else {return}
        
        movies[index].isWatched = true
        saveWatchedMovies()
    }
    
    func nextCurrentMovieIndex() -> Int? {
        currentMovieIndex
    }
    
    private func saveWatchedMovies() {
        let watchedIDs = movies
            .filter(\.isWatched)
            .map(\.id)
        
        UserDefaults.standard.set(watchedIDs, forKey: watchedMoviesKey)
    }
    
    private func loadWatchedMovies() {
        guard let watchedIDs = UserDefaults.standard.stringArray(forKey: watchedMoviesKey) else {
            return
        }
        for index in movies.indices {
            movies[index].isWatched = watchedIDs.contains(movies[index].id)
        }
    }
    
}

