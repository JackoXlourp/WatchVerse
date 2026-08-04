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
            
            if movie.isWatched && centerProgress > 0.5 {
                RoundedRectangle(cornerRadius: 20)
                    .fill(.green.opacity(0.35))
                    .blur(radius: 24)
                    .scaleEffect(1.08)
            }
            
            let posterName = UIImage(named: movie.poster) != nil
                ? movie.poster
                : "placeholder-movie"
            
            Image(posterName)
                .resizable()
                .scaledToFill()
                .saturation(
                    movie.isWatched && centerProgress <= 0.5 ? 0 : 1
                )
                .opacity(
                    movie.isWatched && centerProgress <= 0.5
                        ? 0.65
                        : movie.isSkipped && centerProgress <= 0.5
                            ? 0.80
                            : 1
                )
                .clipShape(
                    RoundedRectangle(
                        cornerRadius: 16 + (4 * centerProgress)
                    )
                )
                .overlay(alignment: .topTrailing) {
                    if movie.isWatched {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 30))
                            .scaleEffect(0.8 + (0.2 * centerProgress))
                            .foregroundStyle(centerProgress > 0.5 ? .green : .white)
                            .opacity(centerProgress > 0.5 ? 1 : 0.6)
                            .shadow(radius: centerProgress > 0.5 ? 8 : 0)
                            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                            .padding(8 + (4 * centerProgress))

                    } else if movie.isSkipped {

                        ZStack {
                            Image(systemName: "forward.fill")
                                .font(.system(size: 30))
                                .foregroundStyle(.black)
                                .offset(x: 1, y: 1)

                            Image(systemName: "forward.fill")
                                .font(.system(size: 30))
                                .foregroundStyle(centerProgress > 0.5 ? .orange : .white)
                        }
                        .scaleEffect(0.8 + (0.2 * centerProgress))
                        .opacity(centerProgress > 0.5 ? 1 : 0.6)
                        .shadow(radius: centerProgress > 0.5 ? 8 : 0)
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                        .padding(8 + (4 * centerProgress))
                    }
                }
            
            RoundedRectangle(
                cornerRadius: 16 + (4 * centerProgress)
            )
            .stroke(
                movie.isWatched && centerProgress > 0.5
                    ? .green
                    : movie.isSkipped && centerProgress > 0.5
                        ? .orange
                        : centerProgress > 0.5
                            ? Color(red: 0.86, green: 0.72, blue: 0.28)
                            : Color.white.opacity(0.12),
                lineWidth:
                    (movie.isWatched || movie.isSkipped) && centerProgress > 0.5
                    ? 3
                    : 2
            )
        }
        .frame(
            width: posterWidth,
            height: posterHeight
        )
        .frame(
            width: carouselCardWidth,
            height: carouselCardHeight
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
