//
//  Movie.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-27.
//

import Foundation

struct Movie: Identifiable {
    let id = UUID()
    let title: String
    let poster: String
}

let marvelMovies: [Movie] = [
    Movie(title: "The Incredible Hulk", poster: "hulk2008"),
    Movie(title: "Iron Man 2", poster: "ironman2"),
    Movie(title: "Guardians of the Galaxy", poster: "guardians1")
]
