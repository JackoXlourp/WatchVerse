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
        VStack {
            Text(movie.title)
                .font(.largeTitle)
                .bold()
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
