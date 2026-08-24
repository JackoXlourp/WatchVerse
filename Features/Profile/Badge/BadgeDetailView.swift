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

        movies.filter {
            authentication.currentUser?.watchedMovies.contains($0.id) == true
        }
        .count
    }
    
    private var isCompleted: Bool {

        completedMovies == movies.count
    }

    var body: some View {

        NavigationStack {

            VStack(spacing: 24) {

                Image(badge.imageName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 160, height: 160)

                Text(badge.title)
                    .font(.title.bold())
                
                if !movies.isEmpty {
                    Text("\(completedMovies) / \(movies.count) completed")
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
