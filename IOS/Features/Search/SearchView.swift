//
//  SearchView.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-25.
//

import SwiftUI

struct SearchView: View {

    @Environment(ContentStore.self) private var contentStore
    @Environment(AuthenticationService.self) private var authentication
    @Environment(JourneyViewModel.self) private var viewModel
    @Environment(AppNavigation.self) private var navigation

    @AppStorage("recentSearches") private var recentSearchesStorage = "[]"

    @State private var query = ""
    @State private var suggestions: [SearchSuggestion] = []
    @State private var submittedResults: [SearchResultGroup] = []
    @State private var submittedQuery: String?
    @State private var isLoadingResults = false

    @FocusState private var isSearchFocused: Bool

    private var trimmedQuery: String {
        query.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private var recentSearches: [String] {
        guard
            let data = recentSearchesStorage.data(using: .utf8),
            let searches = try? JSONDecoder().decode([String].self, from: data)
        else {
            return []
        }

        return searches
    }

    private var isShowingSubmittedResults: Bool {
        submittedQuery == trimmedQuery && !trimmedQuery.isEmpty
    }

    var body: some View {

        VStack(spacing: 24) {

            if !isShowingSubmittedResults {
                Spacer()
                    .frame(height: 50)

                Text("WatchVerse")
                    .font(.system(size: 34, weight: .bold))
                    .foregroundStyle(Color.watchVerseGold)
                    .frame(maxWidth: .infinity)
                    .multilineTextAlignment(.center)
            }

            searchField
                .padding(.top, isShowingSubmittedResults ? 8 : 0)

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 24) {
                    if trimmedQuery.isEmpty {
                        recentSearchesSection
                    } else if isShowingSubmittedResults {
                        submittedResultsSection
                    } else {
                        suggestionsSection
                    }

                    Spacer(minLength: 150)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal)
                .contentShape(Rectangle())
                .onTapGesture {
                    isSearchFocused = false
                }
            }
            .scrollDismissesKeyboard(.immediately)
        }
        .appBackground()
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                SettingsButton()
            }
        }
        .task(id: trimmedQuery) {
            await updateSuggestions()
        }
        .task(id: submittedQuery) {
            guard let submitted = submittedQuery else { return }

            let results = await SearchEngine.results(
                query: submitted,
                contentStore: contentStore,
                user: authentication.currentUser
            )

            guard !Task.isCancelled,
                  submittedQuery == submitted,
                  trimmedQuery == submitted else { return }

            submittedResults = results
            isLoadingResults = false
        }
        .onChange(of: query) { _, newValue in
            if submittedQuery != newValue.trimmingCharacters(in: .whitespacesAndNewlines) {
                submittedQuery = nil
            }
        }
    }

    private var searchField: some View {
        HStack(spacing: 10) {
            Image(systemName: "magnifyingglass")
                .foregroundStyle(.secondary)

            TextField("Search WatchVerse", text: $query)
                .focused($isSearchFocused)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .submitLabel(.search)
                .onSubmit {
                    submitSearch(query)
                }

            if !query.isEmpty {
                Button {
                    query = ""
                    suggestions = []
                    submittedResults = []
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundStyle(.secondary)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 14)
        .frame(height: 44)
        .background(.ultraThinMaterial)
        .clipShape(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
        )
        .padding(.horizontal)
    }

    @ViewBuilder
    private var recentSearchesSection: some View {
        HStack {
            Text("Recent Searches")
                .font(.headline)
                .foregroundStyle(Color.watchVerseGold)

            Spacer()

            if !recentSearches.isEmpty {
                Button("Clear") {
                    recentSearchesStorage = "[]"
                }
                .font(.subheadline)
                .foregroundStyle(Color.watchVerseGold)
            }
        }

        if recentSearches.isEmpty {
            Text("No recent searches yet.")
                .foregroundStyle(.secondary)
        } else {
            ForEach(recentSearches, id: \.self) { search in
                Button {
                    submitSearch(search)
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: "clock.arrow.circlepath")
                            .foregroundStyle(.secondary)

                        Text(search)
                            .foregroundStyle(.white)

                        Spacer()
                    }
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
            }
        }
    }

    @ViewBuilder
    private var suggestionsSection: some View {
        Text("Best Matches")
            .font(.headline)
            .foregroundStyle(Color.watchVerseGold)

        if suggestions.isEmpty {
            Text("No matches yet.")
                .foregroundStyle(.secondary)
        } else {
            ForEach(suggestions) { suggestion in
                Button {
                    openSuggestion(suggestion)
                } label: {
                    suggestionRow(suggestion)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .disabled(navigation.isActivatingUniverse)
            }
        }
    }

    @ViewBuilder
    private var submittedResultsSection: some View {
        if isLoadingResults {
            ProgressView("Searching…")
                .frame(maxWidth: .infinity)
        } else if submittedResults.isEmpty {
            Text("No results for \"\(trimmedQuery)\".")
                .foregroundStyle(.secondary)
        } else {
            LazyVStack(alignment: .leading, spacing: 24) {
                ForEach(submittedResults) { group in
                    resultGroup(group)
                }
            }
        }
    }

    private func suggestionRow(_ suggestion: SearchSuggestion) -> some View {
        HStack(spacing: 14) {
            switch suggestion.kind {
            case .universe:
                ArtworkImageView(
                    source: suggestion.universe.poster,
                    placeholder: "placeholder-poster"
                )
                .scaledToFill()
                .frame(width: 48, height: 48)
                .clipped()
                .clipShape(RoundedRectangle(cornerRadius: 9))

                VStack(alignment: .leading, spacing: 3) {
                    Text(suggestion.universe.fullTitle)
                        .foregroundStyle(.white)
                    Text("Universe")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

            case .content:
                if let item = suggestion.content {
                    ArtworkImageView(
                        source: item.poster,
                        placeholder: "placeholder-movie"
                    )
                    .scaledToFill()
                    .frame(width: 36, height: 54)
                    .clipped()
                    .clipShape(RoundedRectangle(cornerRadius: 6))

                    VStack(alignment: .leading, spacing: 3) {
                        Text(item.title)
                            .foregroundStyle(.white)
                        Text(suggestion.universe.fullTitle)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

            case .badge:
                if let badge = suggestion.badge {
                    ArtworkImageView(
                        source: badge.artwork,
                        placeholder: "placeholder-badge"
                    )
                    .scaledToFit()
                    .frame(width: 44, height: 44)

                    VStack(alignment: .leading, spacing: 3) {
                        Text(badge.title)
                            .foregroundStyle(.white)
                        Text("Badge · \(suggestion.universe.fullTitle)")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            Spacer()
        }
    }

    private func resultGroup(_ group: SearchResultGroup) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Button {
                openUniverse(group.universe)
            } label: {
                HStack(spacing: 12) {
                    ArtworkImageView(
                        source: group.universe.poster,
                        placeholder: "placeholder-poster"
                    )
                    .scaledToFill()
                    .frame(width: 60, height: 60)
                    .clipped()
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                    Text(group.universe.fullTitle)
                        .font(.headline)
                        .foregroundStyle(.white)

                    Spacer()
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .disabled(navigation.isActivatingUniverse)

            ForEach(group.content) { item in
                Button {
                    openUniverse(group.universe, contentID: item.id)
                } label: {
                    HStack(spacing: 14) {
                        ArtworkImageView(
                            source: item.poster,
                            placeholder: "placeholder-movie"
                        )
                        .scaledToFill()
                        .frame(width: 45, height: 68)
                        .clipped()
                        .clipShape(RoundedRectangle(cornerRadius: 7))

                        Text(item.title)
                            .foregroundStyle(.white)

                        Spacer()
                    }
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .disabled(navigation.isActivatingUniverse)
                .padding(.leading, 24)
            }

            ForEach(group.badges) { badge in
                Button {
                    openBadge(badge)
                } label: {
                    HStack(spacing: 14) {
                        ArtworkImageView(
                            source: badge.artwork,
                            placeholder: "placeholder-badge"
                        )
                        .scaledToFit()
                        .frame(width: 45, height: 45)

                        Text(badge.title)
                            .foregroundStyle(.white)

                        Spacer()
                    }
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .disabled(navigation.isActivatingUniverse)
                .padding(.leading, 24)
            }
        }
    }

    private func updateSuggestions() async {
        let search = trimmedQuery

        guard !search.isEmpty else {
            suggestions = []
            return
        }

        try? await Task.sleep(for: .milliseconds(150))
        guard !Task.isCancelled else {
            return
        }

        let matches = await SearchEngine.suggestions(
            query: search,
            contentStore: contentStore,
            user: authentication.currentUser
        )

        guard !Task.isCancelled, trimmedQuery == search else {
            return
        }

        suggestions = matches
    }

    private func submitSearch(_ search: String) {
        let submitted = search.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !submitted.isEmpty else {
            return
        }

        query = submitted
        isSearchFocused = false
        saveRecentSearch(submitted)
        if submittedQuery != submitted {
            submittedResults = []
            isLoadingResults = true
            submittedQuery = submitted
        }
        suggestions = []
    }

    private func openSuggestion(_ suggestion: SearchSuggestion) {
        switch suggestion.kind {
        case .universe:
            openUniverse(suggestion.universe)
        case .content:
            if let item = suggestion.content {
                openUniverse(suggestion.universe, contentID: item.id)
            }
        case .badge:
            if let badge = suggestion.badge {
                openBadge(badge)
            }
        }
    }

    private func openUniverse(_ universe: Universe, contentID: String? = nil) {
        isSearchFocused = false
        Task {
            await navigation.openUniverse(
                universe,
                contentID: contentID,
                contentStore: contentStore,
                authentication: authentication,
                viewModel: viewModel
            )
        }
    }

    private func openBadge(_ badge: Badge) {
        guard contentStore.visibleBadges(for: authentication.currentUser)
            .contains(where: { $0.id == badge.id }) else { return }

        isSearchFocused = false
        navigation.openBadge(id: badge.id)
    }

    private func saveRecentSearch(_ search: String) {
        var updatedSearches = recentSearches.filter {
            $0.compare(search, options: .caseInsensitive) != .orderedSame
        }

        updatedSearches.insert(search, at: 0)
        updatedSearches = Array(updatedSearches.prefix(10))

        guard
            let data = try? JSONEncoder().encode(updatedSearches),
            let storage = String(data: data, encoding: .utf8)
        else {
            return
        }

        recentSearchesStorage = storage
    }
}
