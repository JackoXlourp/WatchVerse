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
    var journeyPositions: [String: String] = [:]

    init(
        showReleaseYears: Bool = true,
        selectedUniverseFilters: [String: Set<String>] = [:],
        journeyPositions: [String: String] = [:]
    ) {
        self.showReleaseYears = showReleaseYears
        self.selectedUniverseFilters = selectedUniverseFilters
        self.journeyPositions = journeyPositions
    }
}
