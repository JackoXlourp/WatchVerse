//
//  Movie.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-27.
//

import Foundation

struct Movie: Identifiable, Hashable {
    let id: String
    
    let title: String
    let poster: String
    
    let year: Int
    let runtime: String
    let phase: String
    
    let synopsis: String
    let director: String
    let genres: [String]
    
    var isWatched: Bool
}

let marvelMovies: [Movie] = [
    Movie(
        id: "hulk-2208",
        title: "The Incredible Hulk",
        poster: "hulk2008",
        year: 2008,
        runtime: "1h 52m",
        phase: "Phase One",
        synopsis: " Coming soon!",
        director: "Uknown",
        genres: [
            "Action",
            "Adventure",
            "Sci-Fi"
        ],
        isWatched: false
    ),
    
    Movie(
        id: "iron-man-2",
        title: "Iron Man 2",
        poster: "ironman2",
        year: 2010,
        runtime: "2h 4m",
        phase: "Phase One",
        synopsis: "Coming Soon!",
        director: "Uknown",
        genres: [
            "Action",
            "Adventure",
            "Sci-Fi"
        ],
        isWatched: false
    ),
    
    Movie(
        id:"guardians-1",
        title: "Guardians of the Galaxy",
        poster: "guardians1",
        year: 2014,
        runtime: "2h 1m",
        phase: "Phase Two",
        synopsis: "Comming Soon!",
        director: "Uknown",
        genres: [
            "Action",
            "Adventure",
            "Sci-Fi"
        ],
        isWatched: false
    )
]
