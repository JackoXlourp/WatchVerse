//
//  HomeView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//

import SwiftUI

struct HomeView: View {
    
    @Environment(JourneyViewModel.self) private var viewModel
    
    init() {
            let appearance = UINavigationBarAppearance()
            appearance.configureWithTransparentBackground()

            appearance.largeTitleTextAttributes = [
                .foregroundColor: UIColor(
                    red: 0.85,
                    green: 0.72,
                    blue: 0.45,
                    alpha: 1
                )
            ]

            appearance.titleTextAttributes = [
                .foregroundColor: UIColor(
                    red: 0.85,
                    green: 0.72,
                    blue: 0.45,
                    alpha: 1
                )
            ]

            UINavigationBar.appearance().standardAppearance = appearance
            UINavigationBar.appearance().scrollEdgeAppearance = appearance
        }
    
    var body: some View {
        
        NavigationStack {
            
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
                            .foregroundStyle(
                                Color(red: 0.85, green: 0.72, blue: 0.45)
                            )
                        
                        HeroUniverseCard(universe: viewModel.journey)
                        
/*                        // MARK: Your Universes
                        
                        Text("Your Universes")
                            .font(.headline)
                            .foregroundStyle(
                                Color(red: 0.85, green: 0.72, blue: 0.45)
                            )
                        
                        
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
                        
*/                        // MARK: Coming Soon!
                        
                        Text("Coming Soon")
                            .font(.headline)
                            .foregroundStyle(
                                Color(red: 0.85, green: 0.72, blue: 0.45)
                            )
                        
                        LazyVGrid(
                            columns: [
                                GridItem(.flexible()),
                                GridItem(.flexible())
                            ],
                            spacing: 20
                        ) {
                            ForEach(comingSoon) { universe in
                                UniverseCard(
                                    universe: universe,
                                    isLocked: true
                                )
                            }
                        }
                        
                    }
                    .padding()
                    .padding(.bottom, 80)
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
}

#Preview {
    HomeView()
        .environment(
            JourneyViewModel(
                journey: universes[0],
                universes: universes
            )
        )
}
