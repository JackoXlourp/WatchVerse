//
//  JourneyView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct JourneyView: View {
    @State private var currentIndex = 0
    @State private var dragOffset: CGSize = .zero
    @State private var selectedMovie: Movie?
    @Environment(JourneyViewModel.self) private var viewModel
    
    private let posterSpacing: CGFloat = 190
    
    var body: some View {
        
        ZStack {
            Color.black
                .ignoresSafeArea()
            
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing:0) {
                    Spacer()
                        .frame(height: 20)
                    
                    HStack {
                        Text(viewModel.journey.title)
                            .font(.system(size: 40, weight: .bold))
                            .fontWeight(.bold)
                            .foregroundStyle(.red)
                        
                        Divider()
                            .frame(height: 20)
                            .overlay(.white)
                        
                        Text(viewModel.journey.subtitle)
                            .font(.system(size: 24, weight: .medium))
                            .foregroundStyle(.white)
                    }
                    
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
                                        selectedMovie = movie
                                    } else {
                                        withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                            currentIndex = index
                                        }
                                    }
                                }
                            )
                            .padding(.top, 60)
                            .offset(x: offset)
                            .zIndex(Double(centerProgress))
                        }
                        //Reset button
                    }
                    .frame(maxWidth: .infinity)
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
                    
                    if !viewModel.isJourneyComplete {
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
                    }
                    
                    Divider()
                        .padding(.vertical, 20)
                        .padding(.top, 35)
                    
                    if viewModel.isJourneyComplete {
                        VStack(spacing: 6) {
                            
                            Text("Journey Completed")
                                .font(.caption)
                                .foregroundStyle(.green)
                                .textCase(.uppercase)
                                .tracking(2)
                            
                            Text(viewModel.journey.fullTitle)
                                .font(.headline)
                                .foregroundStyle(.white)
                            
                        }
                    } else {
                        VStack(spacing: 6) {
                            
                            Text("Journey Progress")
                                .font(.caption)
                                .foregroundStyle(.gray)
                                .textCase(.uppercase)
                                .tracking(2)
                            
                            Text("\(viewModel.watchedCount) of \(viewModel.movieCount)")
                                .font(.headline)
                                .foregroundStyle(.white)
                            
                            Text("\(Int((Double(viewModel.watchedCount) / Double(viewModel.movieCount)) * 100)) % Complete")
                                .font(.subheadline)
                                .foregroundStyle(.gray)
                        }
                    }
                    
                    if viewModel.isJourneyComplete {
                        VStack(spacing: 12) {
                            Image(systemName: "checkmark.seal.fill")
                                .font(.system(size:44))
                                .foregroundStyle(.green)
                            
                            Text("Congratulations!")
                                .font(.title2.bold())
                                .foregroundStyle(.green)
                            
                            Text("You've completed \(viewModel.journey.fullTitle)!")
                                .font(.subheadline)
                                .foregroundStyle(.gray)
                                .multilineTextAlignment(.center)
                        }
                        .padding(.top, 32)
                    }
                    
                    Spacer(minLength: 20)
                }
            }
            
            .padding()
        }
        .onAppear{
            if let index = viewModel.nextCurrentMovieIndex() {
                currentIndex = index
            }
        }
        .sheet(item: $selectedMovie) { movie in
            NavigationStack {
                MovieDetailView(
                    movie: movie,
                    viewModel: viewModel,
                    onMovieWatched: {
                        if let index = viewModel.nextCurrentMovieIndex() {
                            currentIndex = index
                        }
                    }
                )
            }
        }
    }
}
    
    #Preview {
        JourneyView()
    }

