//
//  UserSettings.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-09.
//

import Foundation

struct UserSettings: Codable {
    var showReleaseYears = true
    var selectedUniverseFilters: [String: Set<String>] = [:]
}
