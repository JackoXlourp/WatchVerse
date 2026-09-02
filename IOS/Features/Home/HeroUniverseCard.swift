//
//  HeroUniverseCard.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-31.
//

import SwiftUI

struct HeroUniverseCard: View {

    let universe: Universe
    let onTap: () -> Void
    
    var body: some View {
        
        Button(action: onTap) {

            VStack(alignment: .leading, spacing: 12) {

                Image(universe.banner)
                    .resizable()
                    .scaledToFill()
                    .frame(maxWidth: .infinity, minHeight: 170, maxHeight: 170)
                    .clipped()
                    .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .padding(12)
            .background(Color.white.opacity(0.05))
            .clipShape(RoundedRectangle(cornerRadius: 24))
            .contentShape(RoundedRectangle(cornerRadius: 24))
        }
        .buttonStyle(.plain)
    }
}

