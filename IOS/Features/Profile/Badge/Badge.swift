//
//  Badge.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-10.
//

import Foundation

struct Badge: Identifiable, Equatable, Codable {
    let id: String
    let title: String

    let universeID: String
    let universeTitle: String

    let artwork: String
    let description: String

    let requiredContentIDs: [String]

    let sortOrder: Int
}
