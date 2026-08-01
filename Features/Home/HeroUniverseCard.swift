//
//  HeroUniverseCard.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-31.
//

import SwiftUI

struct HeroUniverseCard: View {
    
    let universe: Universe
    
    var body: some View {
        
        NavigationLink {
            JourneyView()
        } label: {
            
            VStack(alignment: .leading, spacing: 12) {
                
                Image(universe.banner)
                    .resizable()
                    .scaledToFill()
                    .frame(height: 170)
                    .clipped()
                    .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .padding(12)
            .background(Color.white.opacity(0.05))
            .clipShape(RoundedRectangle(cornerRadius: 24))
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    ZStack {
        Color.black
            .ignoresSafeArea()
        
        HeroUniverseCard(universe: universes[0])
            .padding()
    }
}
