//
//  MovieDetailView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-28.
//

import SwiftUI

struct MovieDetailView: View {
    
    let movie: Movie
    let viewModel: JourneyViewModel
    let onMovieWatched: ((String) -> Void)?
    let onMovieSkipped: (() -> Void)?
    
    @Environment(\.dismiss) private var dismiss
    
    private var currentMovie: Movie {
        viewModel.movie(at: viewModel.movies.firstIndex(where: { $0.id == movie.id })!)
    }
    
    var body: some View {
        
        ScrollView {
            
            VStack(alignment: .leading, spacing: 32) {
                
                HStack(alignment: .top, spacing: 20) {
                    
                    Image(currentMovie.poster)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 150)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                    
                    VStack(alignment: .leading, spacing: 12) {
                        
                        Text(currentMovie.title)
                            .font(.title)
                            .fontWeight(.bold)
                        
                        Text(String(currentMovie.year) + "•" + movie.runtime)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                        
                        Text(currentMovie.phase)
                            .font(.caption)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Color.white.opacity(0.15))
                            .clipShape(Capsule())
                    }
                    
                    Spacer()
                }
                
                // MARK: Movie Watched Button
                Button {
                    
                    if currentMovie.isWatched {
                        viewModel.markMovieUnwatched(id: movie.id)
                    } else {
                        onMovieWatched?(movie.id)
                        dismiss()
                    }
                    
                } label: {
                    Label(
                        currentMovie.isWatched ? "Watched" : "Mark as Watched",
                        systemImage: currentMovie.isWatched
                        ? "checkmark.circle.fill"
                        : "circle"
                    )
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(
                            currentMovie.isWatched ? Color.green: Color.yellow
                        )
                        .foregroundStyle(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                
                if !currentMovie.isWatched {

                    // MARK: SKIP BUTTON
                    Button {

                        if currentMovie.isSkipped {
                            viewModel.unskipMovie(id: movie.id)
                        } else {
                            viewModel.skipMovie(id: movie.id)
                            onMovieSkipped?()
                            dismiss()
                        }

                    } label: {
                        Label(
                            currentMovie.isSkipped ? "Unskip" : "Skip",
                            systemImage: currentMovie.isSkipped
                                ? "arrow.uturn.backward"
                                : "forward.fill"
                        )
                        .font(.headline)
                        .frame(width: 140)
                        .padding()
                        .background(Color.gray.opacity(0.25))
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                    }

                }
                
                //MARK: Synopsis....
                VStack(alignment: .leading, spacing: 12) {
                    
                    Text("Synopsis")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text(currentMovie.synopsis)
                        .font(.body)
                        .foregroundStyle(.secondary)
                        .lineSpacing(4)
                }
                
                VStack(alignment: .leading, spacing: 12) {
                    
                    Text("Director")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text(currentMovie.director)
                        .foregroundStyle(.secondary)
                }
                
                VStack(alignment: .leading, spacing: 12) {
                    
                    Text("Genres")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack {
                            ForEach(currentMovie.genres, id: \.self) { genre in
                                Text(genre)
                                    .font(.caption)
                                    .fontWeight(.semibold)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.white.opacity(0.15))
                                    .clipShape(Capsule())
                            }
                        }
                    }
                }
            }
            .padding()
        }
        .navigationTitle(currentMovie.title)
        .navigationBarTitleDisplayMode(.inline)
    }
}


