//
//  splashScreenView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-08-02.
//

import SwiftUI

struct splashScreenView: View {
    
    @State private var showHOme = false
    
    var body: some View {
        
        ZStack {
            
            if showHOme {
                
                RootView()
                
            } else {
                
                Image("wv-launch")
                    .resizable()
                    .scaledToFill()
                    .ignoresSafeArea()
            }
        }
        .task {
            try? await Task.sleep(for: .seconds(0.10))
            
            withAnimation(.easeInOut(duration: 0.4)) {
                showHOme = true
            }
        }
    }
}

#Preview {
    splashScreenView()
}
