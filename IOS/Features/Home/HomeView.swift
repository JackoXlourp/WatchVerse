//
//  HomeView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//

import SwiftUI

struct HomeView: View {
    
    @Environment(JourneyViewModel.self) private var viewModel
    @Environment(AppNavigation.self) private var navigation
    @Environment(ContentStore.self) private var contentStore
    
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
                
                ScrollView(showsIndicators: false) {
                    
                    VStack(alignment: .leading, spacing: 32) {
                        Color.clear
                            .frame(height: 80)
                        
                        // MARK: Continue Watching
                        
                        Text("Continue Watching")
                            .font(.headline)
                            .foregroundStyle(Color.watchVerseGold)
                        
                        HeroUniverseCard(
                            universe: viewModel.journey,
                            onTap: {
                                navigation.selectedTab = .journey
                            }
                        )
                        
                        // MARK: Your Universes
/*
                        Text("Your Universes")
                            .font(.headline)
                            .foregroundStyle(Color.watchVerseGold)
                        
                        
                        LazyVGrid(
                            columns: [
                                GridItem(.flexible()),
                                GridItem(.flexible())
                            ],
                            spacing: 20
                        ) {
                            ForEach(viewModel.universes) { universe in
                                UniverseCard(
                                    universe: universe
                                )
                            }
                        }
*/
                        // MARK: Coming Soon!
                        
                        Text("Coming Soon")
                            .font(.headline)
                            .foregroundStyle(Color.watchVerseGold)
                        
                        LazyVGrid(
                            columns: [
                                GridItem(.flexible()),
                                GridItem(.flexible())
                            ],
                            spacing: 20
                        ) {
                            ForEach(contentStore.comingSoonUniverses) { universe in
                                UniverseCard(
                                    universe: universe,
                                    isLocked: true
                                )
                            }
                        }
                        
                    }
                    .padding()
                    .padding(.bottom, 120)
                }
            }
            .navigationTitle("WatchVerse")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    SettingsButton()
                }
            }
        
    }
}

#Preview {
    let previewUniverse = Universe(
        id: "preview",
        title: "Marvel",
        subtitle: "The Infinity Saga",
        fullTitle: "Marvel Cinematic Universe",
        description: "",
        banner: "placeholder-poster",
        poster: "placeholder-poster",
        filters: nil,
        movies: []
    )

    HomeView()
        .environment(ContentStore())
        .environment(
            JourneyViewModel(
                journey: previewUniverse,
                universes: [previewUniverse]
            )
        )
}
