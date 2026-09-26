//
//  SearchEngine.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-25.
//

import Foundation

@MainActor
struct SearchEngine {

    private enum MatchRank: Int {
        case exactTitle = 0
        case titleStartsWith = 1
        case titleContains = 2
        case tag = 3
        case metadata = 4
    }

    static func results(
        query: String,
        contentStore: ContentStore,
        user: User?
    ) async -> [SearchResultGroup] {

        let searchText = normalized(query)

        guard !searchText.isEmpty else {
            return []
        }

        var rankedGroups: [(group: SearchResultGroup, rank: MatchRank)] = []

        for universe in contentStore.availableUniverses {

            let universeRank = rank(
                title: universe.title,
                alternateTitles: [universe.fullTitle],
                metadata: [universe.subtitle],
                query: searchText
            )

            let content = await contentStore.content(
                for: universe.id
            )

            let matchingContent = content.compactMap { item -> (Movie, MatchRank)? in
                guard let itemRank = rank(
                    title: item.title,
                    alternateTitles: [],
                    metadata: [],
                    tags: item.tags,
                    genres: item.genres,
                    query: searchText
                ) else {
                    return nil
                }

                return (item, itemRank)
            }
            .sorted { first, second in
                if first.1 != second.1 {
                    return first.1.rawValue < second.1.rawValue
                }

                return first.0.timelineOrder < second.0.timelineOrder
            }

            let matchingBadges = contentStore.visibleBadges(for: user).compactMap { badge -> (Badge, MatchRank)? in
                guard
                    badge.universeID == universe.id,
                    let badgeRank = rank(
                        title: badge.title,
                        alternateTitles: [],
                        metadata: [],
                        query: searchText
                    )
                else {
                    return nil
                }

                return (badge, badgeRank)
            }
            .sorted { first, second in
                first.1.rawValue < second.1.rawValue
            }

            let group = SearchResultGroup(
                universe: universe,
                universeMatches: universeRank != nil,
                content: matchingContent.map(\.0),
                badges: matchingBadges.map(\.0)
            )

            if !group.isEmpty {
                var candidateRanks: [MatchRank] = []

                if let universeRank {
                    candidateRanks.append(universeRank)
                }

                if let contentRank = matchingContent
                    .map(\.1)
                    .min(by: { $0.rawValue < $1.rawValue }) {
                    candidateRanks.append(contentRank)
                }

                if let badgeRank = matchingBadges
                    .map(\.1)
                    .min(by: { $0.rawValue < $1.rawValue }) {
                    candidateRanks.append(badgeRank)
                }

                let groupRank = candidateRanks
                    .min(by: { $0.rawValue < $1.rawValue }) ?? .metadata

                rankedGroups.append((group, groupRank))
            }
        }

        return rankedGroups
            .sorted { first, second in
                if first.rank != second.rank {
                    return first.rank.rawValue < second.rank.rawValue
                }

                return first.group.universe.fullTitle < second.group.universe.fullTitle
            }
            .map(\.group)
    }

    static func suggestions(
        query: String,
        contentStore: ContentStore,
        user: User?
    ) async -> [SearchSuggestion] {
        let searchText = normalized(query)
        let groups = await results(query: query, contentStore: contentStore, user: user)

        var suggestions: [SearchSuggestion] = []

        for group in groups {
            if let universeRank = rank(
                title: group.universe.title,
                alternateTitles: [group.universe.fullTitle],
                metadata: [group.universe.subtitle],
                query: searchText
            ) {
                suggestions.append(
                    SearchSuggestion(
                        kind: .universe,
                        universe: group.universe,
                        content: nil,
                        badge: nil,
                        rank: universeRank.rawValue
                    )
                )
            }

            for item in group.content {
                guard let itemRank = rank(
                    title: item.title,
                    alternateTitles: [],
                    metadata: [],
                    tags: item.tags,
                    genres: item.genres,
                    query: searchText
                ) else {
                    continue
                }

                suggestions.append(
                    SearchSuggestion(
                        kind: .content,
                        universe: group.universe,
                        content: item,
                        badge: nil,
                        rank: itemRank.rawValue
                    )
                )
            }

            for badge in group.badges {
                guard let badgeRank = rank(
                    title: badge.title,
                    alternateTitles: [],
                    metadata: [],
                    query: searchText
                ) else {
                    continue
                }

                suggestions.append(
                    SearchSuggestion(
                        kind: .badge,
                        universe: group.universe,
                        content: nil,
                        badge: badge,
                        rank: badgeRank.rawValue
                    )
                )
            }
        }

        return suggestions
            .sorted { first, second in
                if first.rank != second.rank {
                    return first.rank < second.rank
                }

                return first.id < second.id
            }
            .prefix(6)
            .map { $0 }
    }

    private static func normalized(_ value: String) -> String {
        value
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .folding(options: [.caseInsensitive, .diacriticInsensitive], locale: .current)
            .lowercased()
    }

    private static func rank(
        title: String,
        alternateTitles: [String],
        metadata: [String],
        tags: [String] = [],
        genres: [String] = [],
        query: String
    ) -> MatchRank? {
        let titles = [normalized(title)] + alternateTitles.map(normalized)

        if titles.contains(query) {
            return .exactTitle
        }

        if titles.contains(where: { $0.hasPrefix(query) }) {
            return .titleStartsWith
        }

        if titles.contains(where: { $0.localizedCaseInsensitiveContains(query) }) {
            return .titleContains
        }

        if (tags + genres).map(normalized).contains(where: {
            $0.localizedCaseInsensitiveContains(query)
        }) {
            return .tag
        }

        if metadata.map(normalized).contains(where: {
            $0.localizedCaseInsensitiveContains(query)
        }) {
            return .metadata
        }

        return nil
    }
}
