//
//  UniverseCard.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//
import SwiftUI
import UIKit

struct UniverseCard: View {

    let universe: Universe
    var isLocked = false

    private var posterImage: Image {
        guard let image = UIImage(named: universe.poster) else {
            return Image("placeholder-poster")
        }

        return Image(uiImage: image)
    }

    var body: some View {
        
        NavigationLink {
            JourneyView()
        } label: {
            
            VStack(alignment: .leading, spacing: 16) {
                
                posterImage
                    .resizable()
                    .scaledToFill()
                    .aspectRatio(0.68, contentMode: .fit)
                    .frame(maxWidth: .infinity)
                    .saturation(isLocked ? 0 : 1)
                    .clipped()
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .overlay {
                        if isLocked {
                            Image(systemName: "lock.fill")
                                .font(.system(size: 36, weight: .bold))
                                .foregroundStyle(.white)
                                .padding(10)
                                .background(.ultraThinMaterial)
                                .clipShape(Circle())
                                .overlay(
                                    Circle()
                                        .stroke(Color.white.opacity(0.15), lineWidth: 1)
                                )
                        }
                    }
                
                HStack(spacing: 12) {
                    
                    Text(universe.title)
                        .font(.headline)
                        .foregroundStyle(Color.watchVerseGold)
                        .lineLimit(2)
                        .frame(height: 44, alignment: .top)
                    
                    Spacer()
                }
            }
            .padding()
            .frame(maxWidth: .infinity)
            .background(Color.white.opacity(0.05))
            .overlay(
                RoundedRectangle(cornerRadius: 24)
                    .stroke(
                        Color(red: 0.78, green: 0.70, blue: 0.50).opacity(0.35),
                        lineWidth: 1
                    )
            )
            .clipShape(RoundedRectangle(cornerRadius: 24))
            .shadow(
                color: .black.opacity(0.25),
                radius: 8,
                x: 0,
                y: 4
            )
        }
        .buttonStyle(.plain)
        .disabled(isLocked)
    }
}

#Preview {
    ZStack {
        Color.black
            .ignoresSafeArea()

        UniverseCard(universe: universes[0])
            .padding()
    }
}
