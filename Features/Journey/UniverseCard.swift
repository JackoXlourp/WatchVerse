//
//  UniverseCard.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//
import SwiftUI

struct UniverseCard: View {

    let universe: Universe

    var body: some View {
        
        NavigationLink {
            JourneyView()
        } label: {
            
            VStack(alignment: .leading, spacing: 16) {
                
                Image(universe.banner)
                    .resizable()
                    .scaledToFill()
                    .frame(height: 120)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                
                HStack(spacing: 12) {
                    
                    Image(universe.logo)
                        .resizable()
                        .scaledToFit()
                        .frame(height: 28)
                    
                    Text(universe.title)
                        .font(.headline)
                        .foregroundStyle(.white)
                    
                    Spacer()
                }
            }
            .padding()
            .frame(maxWidth: .infinity)
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

        UniverseCard(universe: universes[0])
            .padding()
    }
}
