//
//  ContentStore.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-17.
//

import Foundation
import Observation

@MainActor
@Observable
final class ContentStore {
    private let service = FirestoreContentService()

    var content: [Movie] = []
    var universe: Universe?
    var isLoading = true
    var errorMessage: String?
    var comingSoonUniverses: [Universe] = []
    var badges: [Badge] = []
    
    private var contentCache: [String: [Movie]] = [:]
    
    func loadBadges() async {
        do {
            badges = try await service.fetchBadges()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func loadContent(for universeID: String) async {
        isLoading = false
        errorMessage = nil

        do {
            content = try await service.fetchContent(for: universeID)
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
    
    func loadUniverse(id: String) async {
        isLoading = true
        errorMessage = nil

        do {
            let loadedUniverse = try await service.fetchUniverse(id: id)
            universe = loadedUniverse
            content = loadedUniverse.movies
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
    
    func loadComingSoonUniverses() async {
        do {
            comingSoonUniverses = try await service.fetchComingSoonUniverses()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
    
    func loadInitialContent(universeID: String) async {
        isLoading = true
        errorMessage = nil

        do {
            let loadedUniverse = try await service.fetchUniverse(
                id: universeID
            )

            let loadedComingSoon = try await service.fetchComingSoonUniverses()

            universe = loadedUniverse
            content = loadedUniverse.movies
            comingSoonUniverses = loadedComingSoon
            
            badges = try await service.fetchBadges()
            
            for badge in badges {
                await ArtworkPreloader.preload(badge.artwork)
            }

            let homeArtworkSources = Set(
                [loadedUniverse.banner] +
                loadedComingSoon.map(\.poster)
            )

            for source in homeArtworkSources {
                await ArtworkPreloader.preload(source)
            }

        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
    func content(for universeID: String) async -> [Movie] {

        if universe?.id == universeID {
            return content
        }

        if let cachedContent = contentCache[universeID] {
            return cachedContent
        }

        do {
            let loadedContent = try await service.fetchContent(
                for: universeID
            )

            contentCache[universeID] = loadedContent

            return loadedContent

        } catch {
            errorMessage = error.localizedDescription
            return []
        }
    }
}
