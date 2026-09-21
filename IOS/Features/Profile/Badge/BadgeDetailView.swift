//
//  BadgeDetailView.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-11.
//

import SwiftUI

struct BadgeDetailView: View {

    let badge: Badge
    let movies: [Movie]

    @Environment(AuthenticationService.self)
    private var authentication
    
    @Environment(\.dismiss) private var dismiss
    
    private var completedMovies: Int {
        let availableMovieIDs = Set(movies.map(\.id))

        return badge.requiredContentIDs.filter {
            availableMovieIDs.contains($0) &&
            authentication.currentUser?.watchedMovies.contains($0) == true
        }
        .count
    }
    
    private var isCompleted: Bool {

        completedMovies == badge.requiredContentIDs.count
    }

    var body: some View {

        NavigationStack {

            VStack(spacing: 24) {

                ArtworkImageView(
                    source: badge.artwork,
                    placeholder: "placeholder-badge"
                )
                .scaledToFit()
                .frame(width: 160, height: 160)

                Text(badge.title)
                    .font(.title.bold())
                
                if !badge.requiredContentIDs.isEmpty {
                    Text("\(completedMovies) / \(badge.requiredContentIDs.count) completed")
                        .font(.headline)
                        .foregroundStyle(.secondary)
                }
                
                if isCompleted {
                    Label("Completed", systemImage: "checkmark.seal.fill")
                        .foregroundStyle(Color.watchVerseGold)
                        .font(.headline)
                }

                Text(badge.description)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)

                if !movies.isEmpty {

                    List {
                        
                        if !badge.requiredContentIDs.isEmpty {
                            
                            if movies.isEmpty {
                                
                                VStack(spacing: 12) {
                                    Image(systemName: "clock.fill")
                                        .font(.system(size: 28))
                                        .foregroundStyle(Color.watchVerseGold)
                                    
                                    Text("Coming Soon")
                                        .font(.headline)
                                        .foregroundStyle(.white)
                                    
                                    Text("The required content for this badge is not available in WatchVerse yet.")
                                        .font(.subheadline)
                                        .foregroundStyle(.secondary)
                                        .multilineTextAlignment(.center)
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 20)
                                
                            } else {
                                
                                Section("Movies") {
                                    
                                    ForEach(movies) { movie in
                                        
                                        HStack {
                                            
                                            Text(movie.title)
                                            
                                            Spacer()
                                            
                                            Image(systemName:
                                                    authentication.currentUser?.watchedMovies.contains(movie.id) == true
                                                  ? "checkmark.square.fill"
                                                  : "square"
                                            )
                                            .foregroundStyle(
                                                authentication.currentUser?.watchedMovies.contains(movie.id) == true
                                                ? Color.watchVerseGold
                                                : .secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer()
            }
            .padding()
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark")
                    }
                }
            }
        }
    }
}
