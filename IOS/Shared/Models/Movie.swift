//
//  Movie.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-27.
//

import Foundation

enum MovieReleaseStatus: String, Codable {
    case released
    case comingSoon
}

struct Movie: Identifiable, Hashable, Codable {
    let id: String
    
    let title: String
    let poster: String
    
    let year: Int
    let runtime: String
    
    let synopsis: String
    let director: String
    let genres: [String]
    
    var tags: [String] = []
    let releaseStatus: MovieReleaseStatus
    
    var isWatched: Bool
    var isSkipped: Bool
    
    private enum CodingKeys: String, CodingKey {
        case id, title, poster, year, runtime, synopsis, director, genres, tags, releaseStatus, isWatched, isSkipped
    }

    init(
        id: String,
        title: String,
        poster: String,
        year: Int,
        runtime: String,
        synopsis: String,
        director: String,
        genres: [String],
        tags: [String] = [],
        releaseStatus: MovieReleaseStatus = .released,
        isWatched: Bool,
        isSkipped: Bool
    ) {
        self.id = id
        self.title = title
        self.poster = poster
        self.year = year
        self.runtime = runtime
        self.synopsis = synopsis
        self.director = director
        self.genres = genres
        self.tags = tags
        self.releaseStatus = releaseStatus
        self.isWatched = isWatched
        self.isSkipped = isSkipped
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)

        id = try container.decode(String.self, forKey: .id)
        title = try container.decode(String.self, forKey: .title)
        poster = try container.decode(String.self, forKey: .poster)
        year = try container.decode(Int.self, forKey: .year)
        runtime = try container.decode(String.self, forKey: .runtime)
        synopsis = try container.decode(String.self, forKey: .synopsis)
        director = try container.decode(String.self, forKey: .director)
        genres = try container.decode([String].self, forKey: .genres)
        tags = try container.decodeIfPresent([String].self, forKey: .tags) ?? []
        releaseStatus = try container.decodeIfPresent(MovieReleaseStatus.self, forKey: .releaseStatus) ?? .released
        isWatched = try container.decode(Bool.self, forKey: .isWatched)
        isSkipped = try container.decode(Bool.self, forKey: .isSkipped)
    }
}
