//
//  Movie.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-27.
//

import Foundation

struct Movie: Identifiable, Hashable, Codable {
    let id: String
    
    let title: String
    let poster: String
    
    let year: Int
    let runtime: String
    
    let synopsis: String
    let director: String
    let genres: [String]
    
    var isWatched: Bool
    var isSkipped: Bool
}

