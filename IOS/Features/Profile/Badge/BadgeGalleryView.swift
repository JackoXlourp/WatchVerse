//
//  BadgeGalleryView.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-10.
//

import SwiftUI

struct BadgeGalleryView: View {
    
    @Environment(AuthenticationService.self)
    private var authentication
    
    @Environment(ContentStore.self) private var contentStore
    
    @State private var selectedBadge: Badge?
    @State private var selectedBadgeMovies: [Movie] = []
    
    
    private let columns = [
        GridItem(.flexible()),
        GridItem(.flexible()),
        GridItem(.flexible())
    ]
    
    private var badges: [Badge] {
        contentStore.badges.filter { badge in
            badge.id != "founder" ||
            authentication.currentUser?.isFounder == true
        }
    }
    
    private func isUnlocked(_ badge: Badge) -> Bool {
        if badge.id == "founder" {
            return authentication.currentUser?.isFounder == true
        }

        return authentication.currentUser?
            .unlockedBadges
            .contains(badge.id) ?? false
    }
    
    var body: some View {
        
        ZStack {
            
            Image("AppBackground")
                .resizable()
                .scaledToFill()
                .frame(
                    width: UIScreen.main.bounds.width,
                    height: UIScreen.main.bounds.height
                )
                .clipped()
                .ignoresSafeArea()
            
            ScrollView {
                VStack(alignment: .leading, spacing: 40) {
                    
                    Spacer()
                        .frame(height: 50)
                    
                    let groupedBadges = Dictionary(grouping: badges) { badge in
                        badge.universeTitle
                    }

                    ForEach(
                        groupedBadges.keys.sorted {
                            if $0 == "WATCHVERSE" {
                                return true
                            }
                            
                            if $1 == "WATCHVERSE" {
                                return false
                            }
                            
                            return $0 < $1
                        },
                        id: \.self
                    ) { universe in
                        
                        if let universeBadges = groupedBadges[universe] {
                            
                            badgeSection(title: universe) {
                                
                                LazyVGrid(columns: columns, spacing: 28) {
                                    
                                    ForEach(universeBadges) { badge in
                                        
                                        BadgeCardView(
                                            title: badge.title,
                                            artwork: badge.artwork,
                                            isUnlocked: isUnlocked(badge)
                                        )
                                        .onTapGesture {
                                            selectedBadgeMovies = []
                                            selectedBadge = badge
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                .padding()
                .padding(.bottom, 150)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    SettingsButton()
                }
            }
            .sheet(item: $selectedBadge) { badge in
                BadgeDetailView(
                    badge: badge,
                    movies: []
                )
                .task {
                    let loadedContent = await contentStore.content(
                        for: badge.universeID
                    )

                    selectedBadgeMovies = loadedContent
                        .filter {
                            badge.requiredContentIDs.contains($0.id)
                        }
                        .sorted { first, second in

                            let firstWatched =
                                authentication.currentUser?
                                    .watchedMovies
                                    .contains(first.id) == true

                            let secondWatched =
                                authentication.currentUser?
                                    .watchedMovies
                                    .contains(second.id) == true

                            return firstWatched == false &&
                                   secondWatched == true
                        }
                }
            }
        }
    }
    
    private func badgeSection<Content: View>(
        title: String,
        @ViewBuilder content: () -> Content
    ) -> some View {
        
        VStack(alignment: .leading, spacing: 16) {
            
            Text(title)
                .font(.system(size: 20, weight: .bold))
                .tracking(1.5)
                .foregroundStyle(.white)
            
            content()
        }
    }
}
    
struct BadgeCardView: View {
    
    let title: String
    let artwork: String
    let isUnlocked: Bool
    
    var body: some View {
        
        VStack(spacing: 12) {
            
            ZStack {

                ArtworkImageView(
                    source: artwork,
                    placeholder: "placeholder-badge"
                )
                .scaledToFit()
                .frame(width: 110, height: 110)
                .grayscale(isUnlocked ? 0 : 1)

                if isUnlocked {
                    Image(systemName: "checkmark.seal.fill")
                        .font(.system(size: 28))
                        .foregroundStyle(Color.watchVerseGold)
                        .offset(x: 38, y: 38)
                }
            }
            
            Text(title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(
                    isUnlocked
                    ? .white
                    : .gray
                )
                .multilineTextAlignment(.center)
        }
    }
}

