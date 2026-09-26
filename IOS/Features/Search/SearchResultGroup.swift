//
//  SearchResultGroup.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-25.
//

import Foundation

struct SearchResultGroup: Identifiable {
    let universe: Universe
    let universeMatches: Bool
    let content: [Movie]
    let badges: [Badge]
    
    var id: String {
        universe.id
    }
    
    var isEmpty: Bool {
        !universeMatches &&
        content.isEmpty &&
        badges.isEmpty
    }
}

struct SearchSuggestion: Identifiable {

    enum Kind: String {
        case universe
        case content
        case badge
    }

    let kind: Kind
    let universe: Universe
    let content: Movie?
    let badge: Badge?
    let rank: Int

    var id: String {
        switch kind {
        case .universe:
            return "universe-\(universe.id)"
        case .content:
            return "content-\(universe.id)-\(content?.id ?? "")"
        case .badge:
            return "badge-\(badge?.id ?? "")"
        }
    }
}
