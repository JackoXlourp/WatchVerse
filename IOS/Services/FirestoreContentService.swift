//
//  FirestoreContentService.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-17.
//

import Foundation
import FirebaseFirestore

struct FirestoreContentService {
    private let db = Firestore.firestore()

    func fetchContent(for universeID: String) async throws -> [Movie] {
        let snapshot = try await db
            .collection("universes")
            .document(universeID)
            .collection("content")
            .order(by: "timelineOrder")
            .getDocuments()

        return try snapshot.documents.map { document in
            try document.data(as: Movie.self)
        }
    }
    
    func fetchComingSoonUniverses() async throws -> [Universe] {
        let snapshot = try await db
            .collection("universes")
            .order(by: "sortOrder")
            .getDocuments()

        return snapshot.documents.compactMap { document in
            let data = document.data()

            guard
                data["state"] as? String == "comingSoon",
                let title = data["title"] as? String,
                let subtitle = data["subtitle"] as? String,
                let fullTitle = data["fullTitle"] as? String,
                let description = data["description"] as? String,
                let banner = data["banner"] as? String,
                let poster = data["poster"] as? String
            else {
                return nil
            }

            return Universe(
                id: document.documentID,
                title: title,
                subtitle: subtitle,
                fullTitle: fullTitle,
                description: description,
                banner: banner,
                poster: poster,
                filters: data["filters"] as? [String],
                movies: []
            )
        }
    }
    
    func fetchUniverse(id: String) async throws -> Universe {
        let document = try await db
            .collection("universes")
            .document(id)
            .getDocument()

        guard
            let data = document.data(),
            let title = data["title"] as? String,
            let subtitle = data["subtitle"] as? String,
            let fullTitle = data["fullTitle"] as? String,
            let description = data["description"] as? String,
            let banner = data["banner"] as? String,
            let poster = data["poster"] as? String
        else {
            throw NSError(
                domain: "FirestoreContentService",
                code: 1,
                userInfo: [NSLocalizedDescriptionKey: "Invalid universe data"]
            )
        }

        let movies = try await fetchContent(for: id)

        return Universe(
            id: id,
            title: title,
            subtitle: subtitle,
            fullTitle: fullTitle,
            description: description,
            banner: banner,
            poster: poster,
            filters: data["filters"] as? [String],
            movies: movies
        )
    }
}
