//
//  MoviePosterView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-27.
//

import SwiftUI

struct MoviePosterView: View {
    let movie: Movie
    let centerProgress: CGFloat
    let onTap: () -> Void
    
    private var posterWidth: CGFloat {
        140 + (80 * centerProgress)
    }
    
    private var posterHeight: CGFloat {
        215 + (110 * centerProgress)
    }
    
    var body: some View {
        
        ZStack {
            
            Image(movie.poster)
                .resizable()
                .scaledToFill()
                .frame(
                    width: posterWidth,
                    height: posterHeight
                )
                .clipShape(
                    RoundedRectangle(
                        cornerRadius: 16 + (4 * centerProgress)
                    )
                )
            
            RoundedRectangle(
                cornerRadius: 16 + (4 * centerProgress)
            )
            .stroke(
                centerProgress > 0.5
                ? Color(red:0.86, green: 0.72, blue: 0.28)
                : Color.white.opacity(0.12),
            )
            .frame(
                width: posterWidth,
                height: posterHeight
            )
        }
        .frame(
            width: 190,
            height: 275
        )
        .shadow(
            color: .black.opacity(0.45 * centerProgress),
            radius: 18,
            x: 0,
            y: 12
        )
        .zIndex(centerProgress)
        .onTapGesture {
            onTap()
        }
    }
}
