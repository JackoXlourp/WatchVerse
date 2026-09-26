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
    var availableUniverses: [Universe] = []
    var comingSoonUniverses: [Universe] = []
    var badges: [Badge] = []
    
    private var contentCache: [String: [Movie]] = [:]

    func visibleBadges(for user: User?) -> [Badge] {
        badges.filter {
            $0.id != "founder" || user?.isFounder == true
        }
    }
    
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
    
    func preloadArtwork(for universe: Universe) async {

        let sources = Set(
            [universe.banner, universe.poster]
            + universe.movies.map(\.poster)
        )

        await withTaskGroup(of: Void.self) { group in

            for source in sources {
                group.addTask {
                    await ArtworkPreloader.preload(source)
                }
            }

            await group.waitForAll()
        }
    }
    
    func loadCatalogWithoutSelectedUniverse() async {
        isLoading = true
        errorMessage = nil

        do {
            let loadedAvailable =
                try await service.fetchAvailableUniverses()

            let loadedComingSoon =
                try await service.fetchComingSoonUniverses()

            let loadedBadges =
                try await service.fetchBadges()

            universe = nil
            content = []
            availableUniverses = loadedAvailable
            comingSoonUniverses = loadedComingSoon
            badges = loadedBadges

            for badge in loadedBadges {
                await ArtworkPreloader.preload(
                    badge.artwork
                )
            }

            let artworkSources = Set(
                loadedAvailable.map(\.poster) +
                loadedComingSoon.map(\.poster)
            )

            for source in artworkSources {
                await ArtworkPreloader.preload(
                    source
                )
            }

        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
    
    func loadInitialContent(universeID: String) async {
        isLoading = true
        errorMessage = nil

        do {
            let loadedUniverse = try await service.fetchUniverse(
                id: universeID
            )

            let loadedAvailable = try await service.fetchAvailableUniverses()
            let loadedComingSoon = try await service.fetchComingSoonUniverses()

            universe = loadedUniverse
            content = loadedUniverse.movies
            availableUniverses = loadedAvailable
            comingSoonUniverses = loadedComingSoon
            
            await preloadArtwork(for: loadedUniverse)
            
            badges = try await service.fetchBadges()
            
            for badge in badges {
                await ArtworkPreloader.preload(badge.artwork)
            }

            let homeArtworkSources = Set(
                [loadedUniverse.banner, loadedUniverse.poster] +
                loadedAvailable.map(\.poster) +
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
