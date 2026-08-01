//
//  Journey.swift
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
    
    let logo: String
    let banner: String
    let poster: String
    
    let movies: [Movie]
}
