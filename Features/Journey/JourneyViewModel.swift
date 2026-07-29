//
//  JourneyViewModel.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-29.
//

import SwiftUI

@Observable
class JourneyViewModel {
    
    var movies = marvelMovies
    
    var movieCount: Int {
        movies.count
    }
    
    var nextMovie: Movie? {
        nil
    }
    
    func movie(at index: Int) -> Movie {
        movies[index]
    }
    
    var watchedCount: Int {
        movies.filter(\.isWatched).count
    }
    
    func markMovieWatched(id: UUID) {
        guard let index = movies.firstIndex(where: { $0.id == id }) else {return}
        movies[index].isWatched = true
    }
}

