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
    @State private var path = NavigationPath()
    @State private var viewModel = JourneyViewModel()
    
    private let posterSpacing: CGFloat = 190
    
    var body: some View {
        
        NavigationStack(path: $path) {
            
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
                            ForEach(Array(viewModel.movies.enumerated()), id: \.element.id) { index, movie in
                                
                                let offset = CGFloat(index - currentIndex) * posterSpacing + dragOffset.width
                                
                                let centerProgress = max(
                                    0,
                                    1 - abs(offset) / posterSpacing
                                )
                                
                                MoviePosterView(
                                    movie: movie,
                                    centerProgress: centerProgress,
                                    onTap: {
                                        if index == currentIndex {
                                            path.append(movie)
                                        } else {
                                            withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                                currentIndex = index
                                            }
                                        }
                                    }
                                )
                                .offset(x: offset)
                            }
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
                                            viewModel.movieCount - 1
                                        )
                                    )
                                    
                                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                        currentIndex = newIndex
                                        dragOffset = .zero
                                    }
                                }
                        )
                    }
                    .frame(maxWidth: .infinity)
                    
                    VStack(spacing: 8) {
                        
                        Text("Current Movie")
                            .font(.caption)
                            .foregroundStyle(.gray)
                            .textCase(.uppercase)
                            .tracking(2)
                        
                        
                        
                        Text(viewModel.movie(at: currentIndex).title)
                            .font(.title2.weight(.bold))
                            .foregroundStyle(.white)
                            .multilineTextAlignment(.center)
                        
                        Text("\(String(viewModel.movies[currentIndex].year)) • \(viewModel.movies[currentIndex].runtime)")
                            .font(.subheadline)
                            .foregroundStyle(.gray)
                        
                        Text(viewModel.movies[currentIndex].phase)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Color.white.opacity(0.15))
                            .clipShape(Capsule())
                    }
                    .padding(.top, 40)
                    .id(currentIndex)
                    .transition(.opacity)
                    .animation(.easeInOut(duration: 0.25), value: currentIndex)
                    
                    Divider()
                        .padding(.vertical, 20)
                    
                    VStack(spacing: 6) {
                        
                        Text("Journey Progress")
                            .font(.caption)
                            .foregroundStyle(.gray)
                            .textCase(.uppercase)
                            .tracking(2)
                        
                        Text("\(currentIndex + 1) of \(viewModel.movieCount)")
                            .font(.headline)
                            .foregroundStyle(.white)
                        
                        Text("\(Int((Double(currentIndex + 1) / Double(viewModel.movieCount)) * 100)) % Complete")
                            .font(.subheadline)
                            .foregroundStyle(.gray)
                        
                    }
                    
                    Spacer()
                }
                
                .padding()
            }
            .navigationDestination(for: Movie.self) { movie in
                MovieDetailView(movie: movie)
            }
        }
    }
}
    
    #Preview {
        JourneyView()
    }

