//
//  UniverseCatalogItem.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//

import Foundation

struct UniverseCatalogItem: Codable {
    let id: String
    let file: String
    let state: UniverseState
}

enum UniverseState: String, Codable {
    case available
    case comingSoon
}
