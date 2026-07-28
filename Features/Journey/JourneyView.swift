//
//  JourneyView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct JourneyView: View {
    @State private var currentIndex = 1
    @State private var dragOffset: CGSize = .zero
    
    private let posterSpacing: CGFloat = 190
    
    var body: some View {
        ZStack {
            Color.black
                .ignoresSafeArea()
            VStack {
                HStack {
                    Text("Marvel")
                        .font(.system(size: 40, weight: .bold))
                        .fontWeight(.bold)
                        .foregroundStyle(.red)
                    
                    Divider()
                        .frame(height: 20)
                        .overlay(.white)
                    
                    Text("Cinematic Universe")
                        .font(.system(size: 24, weight: .medium))
                        .foregroundStyle(.white)
                }
                
                Spacer()
                    .frame(height: 100)
                
                VStack {
                    ZStack {
                        ForEach(Array(marvelMovies.enumerated()), id: \.element.id) { index, movie in
                            
                            let offset = CGFloat(index - currentIndex) * posterSpacing + dragOffset.width
                            
                            let centerProgress = max(
                                0,
                                1 - abs(offset) / posterSpacing
                            )
                            
                            MoviePosterView(
                                movie: movie,
                                centerProgress: centerProgress
                            )
                            .offset(x: offset)
                        }
                    }
                }
                .frame(maxWidth: .infinity)
                
                
                Spacer()
            }
            .gesture(
                DragGesture()
                    .onChanged{ value in
                        dragOffset = value.translation
                    }
                
                .onEnded { value in
                    
                    let movement = Int((-value.translation.width / posterSpacing).rounded())
                    
                    let newIndex = max(
                        0,
                        min(
                            currentIndex + movement,
                            marvelMovies.count - 1
                        )
                    )
                    
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                        currentIndex = newIndex
                        dragOffset = .zero
                    }
                }
            )
            .padding()
        }
    }
}

#Preview {
    JourneyView()
}
