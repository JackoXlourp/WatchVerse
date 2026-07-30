//
//  JourneyData.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//

import Foundation

let catalog = JSONLoader.load("universes", as: [UniverseCatalogItem].self)

let availableUniverses = catalog.filter { $0.state == .available}

let comingSoonUniverses = catalog.filter { $0.state == .comingSoon}

let universes = availableUniverses.map {
    JSONLoader.load($0.file, as: Universe.self)
}
