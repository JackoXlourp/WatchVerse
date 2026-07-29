//
//  MovieDetailView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-28.
//

import SwiftUI

struct MovieDetailView: View {
    
    let movie: Movie
    
    var body: some View {
        
        ScrollView {
            
            VStack(alignment: .leading, spacing: 32) {
                
                HStack(alignment: .top, spacing: 20) {
                    
                    Image(movie.poster)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 150)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                    
                    VStack(alignment: .leading, spacing: 12) {
                        
                        Text(movie.title)
                            .font(.title)
                            .fontWeight(.bold)
                        
                        Text(String(movie.year) + "•" + movie.runtime)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                        
                        Text(movie.phase)
                            .font(.caption)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Color.white.opacity(0.15))
                            .clipShape(Capsule())
                    }
                    
                    Spacer()
                }
                
                Button {
                    
                } label: {
                    Label("Mark as Watched", systemImage: "chechmark.circle.fill")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.yellow)
                        .foregroundStyle(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                
                VStack(alignment: .leading, spacing: 12) {
                    
                    Text("Synopsis")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text(movie.synopsis)
                        .font(.body)
                        .foregroundStyle(.secondary)
                        .lineSpacing(4)
                }
                
                VStack(alignment: .leading, spacing: 12) {
                    
                    Text("DIrector")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text(movie.director)
                        .foregroundStyle(.secondary)
                }
                
                VStack(alignment: .leading, spacing: 12) {
                    
                    Text("Genres")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack {
                            ForEach(movie.genres, id: \.self) { genre in
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
        .navigationTitle(movie.title)
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    NavigationStack {
        MovieDetailView(movie: marvelMovies[0])
    }
}
