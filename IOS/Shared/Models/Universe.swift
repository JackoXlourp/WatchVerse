//
//  Universe.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//

import Foundation

struct Universe: Identifiable, Codable {
    let id: String
    
    let title: String
    let subtitle: String
    let fullTitle: String
    
    let description: String
    
    let banner: String
    let poster: String
    
    let filters: [String]?
    
    let movies: [Movie]
    
    static let loading = Universe(
        id: "loading",
        title: "",
        subtitle: "",
        fullTitle: "",
        description: "",
        banner: "",
        poster: "",
        filters: nil,
        movies: []
    )
}
