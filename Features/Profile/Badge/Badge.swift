//
//  Badge.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-10.
//

import Foundation

struct Badge: Identifiable {
    let id: String
    let title: String
    let universe: String
    let imageName: String
    let description: String
    let isUnlocked: Bool
    let requiredMovieIDs: [String]
}
