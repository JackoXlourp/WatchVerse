//
//  HomeView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//

import SwiftUI

struct HomeView: View {
    
    @Environment(JourneyViewModel.self) private var viewModel
    
    var nextMovie: Movie? {
        viewModel.nextMovie
    }
    
    var watchedCount: Int {
        viewModel.movies.filter(\.isWatched).count
    }
    
    var body: some View {
    
        NavigationStack {
            ZStack {
                Color.black
                    .ignoresSafeArea()
                
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 32) {
                        
                        Text("WatchVerse")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundStyle(.white)
                        
                        Text("Continue Journey")
                            .font(.headline)
                            .foregroundStyle(.white)
                        
                        NavigationLink {
                            JourneyView()
                        } label: {
                            VStack {
                                HStack(alignment:.top, spacing: 16) {
                                    
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(.gray.opacity(0.3))
                                        .frame(width: 120, height: 180)
                                    
                                    VStack(alignment: .leading, spacing: 8) {
                                        
                                        Text(viewModel.journey.fullTitle)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                        
                                        Text(nextMovie?.title ?? "Journey Complete")
                                            .font(.headline)
                                            .fontWeight(.semibold)
                                            .foregroundStyle(.white)
                                        
                                        Text("\(watchedCount) / \(viewModel.journey.movies.count) Completed")
                                            .font(.caption)
                                            .foregroundStyle(.white)
                                        
                                    }
                                    
                                    Spacer()
                                }
                                .padding()
                                
                            }
                            .frame(height: 240)
                            .frame(maxWidth: .infinity)
                            .background(
                                Color(.systemGray6).opacity(0.08)
                            )
                            .clipShape(
                                RoundedRectangle(cornerRadius: 20)
                            )
                        }
                        .buttonStyle(.plain)
                    }
                    .padding()
                }
            }
        }
    }
}

#Preview {
    HomeView()
        .environment(JourneyViewModel(journey: marvelJourney))
}
