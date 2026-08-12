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
    
    @State private var selectedBadge: Badge?
    
    private let columns = [
        GridItem(.flexible()),
        GridItem(.flexible()),
        GridItem(.flexible())
    ]
    
    private var badges: [Badge] {
        
        BadgeData.all
            .filter { badge in
                badge.id != "founder" || authentication.currentUser?.isFounder == true
            }
            .map { badge in
                
                if badge.id == "founder" {
                    return Badge(
                        id: badge.id,
                        title: badge.title,
                        universe: badge.universe,
                        imageName: badge.imageName,
                        description: badge.description,
                        isUnlocked: true,
                        requiredMovieIDs: badge.requiredMovieIDs
                    )
                }
                
                return Badge(
                    id: badge.id,
                    title: badge.title,
                    universe: badge.universe,
                    imageName: badge.imageName,
                    description: badge.description,
                    isUnlocked: authentication.currentUser?.unlockedBadges.contains(badge.id) ?? false,
                    requiredMovieIDs: badge.requiredMovieIDs
                )
            }
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
                    
                    let watchVerseBadges = badges.filter {
                        $0.universe == "WATCHVERSE"
                    }
                    
                    let marvelBadges = badges.filter {
                        $0.universe == "MARVEL"
                    }
                    
                    let lotrBadges = badges.filter {
                        $0.universe == "LORD OF THE RINGS"
                    }
                    //MARK: WATCHVERSE
                    if !watchVerseBadges.isEmpty {
                        
                        badgeSection(title: "WATCHVERSE") {
                            
                            LazyVGrid(columns: columns, spacing: 28) {
                                
                                ForEach(watchVerseBadges) { badge in
                                    
                                    BadgeCardView(
                                        title: badge.title,
                                        imageName: badge.imageName,
                                        isUnlocked: badge.isUnlocked
                                    )
                                    .onTapGesture {
                                        selectedBadge = badge
                                    }
                                }
                            }
                        }
                    }
                    //MARK: MARVEL
                    if !marvelBadges.isEmpty {
                        
                        badgeSection(title: "MARVEL") {
                            
                            LazyVGrid(columns: columns, spacing: 28) {
                                
                                ForEach(marvelBadges) { badge in
                                    
                                    BadgeCardView(
                                        title: badge.title,
                                        imageName: badge.imageName,
                                        isUnlocked: badge.isUnlocked
                                    )
                                    .onTapGesture {
                                        selectedBadge = badge
                                    }
                                }
                            }
                        }
                    }
                    //MARK: LOTR
                    if !lotrBadges.isEmpty {
                        
                        badgeSection(title: "LORD OF THE RINGS") {
                            
                            LazyVGrid(columns: columns, spacing: 28) {
                                
                                ForEach(lotrBadges) { badge in
                                    
                                    BadgeCardView(
                                        title: badge.title,
                                        imageName: badge.imageName,
                                        isUnlocked: badge.isUnlocked
                                    )
                                    .onTapGesture {
                                        selectedBadge = badge
                                    }
                                }
                            }
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Badges")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    SettingsButton()
                }
            }
            .sheet(item: $selectedBadge) { badge in
                BadgeDetailView(
                    badge: badge,
                    movies: moviesForBadge(badge)
                )
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
    
    private func moviesForBadge(_ badge: Badge) -> [Movie] {

        let allMovies = universes.flatMap { $0.movies }

        return allMovies
            .filter { movie in
                badge.requiredMovieIDs.contains(movie.id)
            }
            .sorted { first, second in

                let firstWatched = authentication.currentUser?.watchedMovies.contains(first.id) == true
                let secondWatched = authentication.currentUser?.watchedMovies.contains(second.id) == true

                return firstWatched == false && secondWatched == true
            }
    }
    
}
    
struct BadgeCardView: View {
    
    let title: String
    let imageName: String
    let isUnlocked: Bool
    
    var body: some View {
        
        VStack(spacing: 12) {
            
            ZStack {

                Image(imageName)
                    .resizable()
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

