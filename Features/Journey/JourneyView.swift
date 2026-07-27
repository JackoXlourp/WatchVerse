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
            }
            .padding()
        }
    }
}

#Preview {
    JourneyView()
}
