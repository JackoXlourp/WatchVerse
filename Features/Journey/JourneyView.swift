//
//  JourneyView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct JourneyView: View {
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
                        
                        Image("guardians1")
                            .resizable()
                            .scaledToFill()
                            .frame(width: 120, height: 185)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                    
                        Image("ironman2")
                            .resizable()
                            .scaledToFill()
                            .frame(width: 190, height: 275)
                            .clipShape(RoundedRectangle(cornerRadius: 20))
                            .contentShape(RoundedRectangle(cornerRadius: 16))
                            .zIndex(1)
                            .shadow(color: .black.opacity(0.45), radius: 18, x:0, y: 12)
                    
                        Image("hulk2008")
                            .resizable()
                            .scaledToFill()
                            .frame(width: 120, height: 185)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
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
