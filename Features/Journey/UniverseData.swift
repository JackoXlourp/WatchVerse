//
//  JourneyData.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//

import Foundation

let catalog = JSONLoader.load("universes", as: [UniverseCatalogItem].self)

let universes = catalog.map {
    JSONLoader.load($0.file, as: Universe.self)
}
