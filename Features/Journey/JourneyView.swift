//
//  JourneyView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct JourneyView: View {
    @State private var currentIndex = 1
    
    private var currentMovie: Movie {
        marvelMovies[currentIndex]
    }
    
    private var leftMovie: Movie? {
        guard currentIndex > 0 else { return nil }
        return marvelMovies[currentIndex - 1]
    }
    
    private var rightMovie: Movie? {
        guard currentIndex < marvelMovies.count - 1 else { return nil }
        return marvelMovies[currentIndex + 1]
    }
    
    var body: some View {
        ZStack {
            Color.black
                .ignoresSafeArea()
            VStack {
                HStack {
                    Text("Marvel")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundStyle(.red)
                    
                    Divider()
                        .frame(height: 20)
                        .overlay(.white)
                    
                    Text("Cinematic Universe")
                        .font(.title2)
                        .foregroundStyle(.white)
                }
                
                Spacer()
                    .frame(height: 100)
                
                VStack {
                    HStack(spacing: -20) {
                        
                        if let leftMovie {
                            Image(leftMovie.poster)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 120, height: 185)
                                .clipped()
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 16)
                                        .stroke(Color.white.opacity(0.12), lineWidth: 1)
                                )
                        } else {
                            Color.clear
                                .frame(width: 120, height: 185)
                        }
                    
                        Image(currentMovie.poster)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 190, height: 275)
                            .clipped()
                            .clipShape(RoundedRectangle(cornerRadius: 20))
                            .overlay(
                                RoundedRectangle(cornerRadius: 20)
                                    .stroke(Color(red: 0.86, green: 0.72, blue: 0.28), lineWidth: 1.5)
                            )
                            .contentShape(RoundedRectangle(cornerRadius: 16))
                            .zIndex(1)
                            .shadow(color: .black.opacity(0.45), radius: 18, x:0, y: 12)
                    
                        if let rightMovie {
                            Image(rightMovie.poster)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 120, height: 185)
                                .clipped()
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 16)
                                        .stroke(Color.white.opacity(0.12), lineWidth: 1)
                                )
                        } else {
                            Color.clear
                                .frame(width: 120, height: 185)
                        }
                    }
                }
                .frame(maxWidth: .infinity)
                
                Spacer()
            }
            .padding()
        }
    }
}

#Preview {
    JourneyView()
}
