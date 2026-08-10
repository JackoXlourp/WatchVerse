//
//  Untitled.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-09.
//

import Foundation

struct User: Codable {

    let userID: String

    var displayName: String

    let joinedDate: Date

    var isFounder: Bool

    var watchedMovies: [String]

    var skippedMovies: [String]

    var settings: UserSettings

}

