//
//  ContentView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        ZStack {
            Color.black
                .ignoresSafeArea()
            
            Text("WatchVerse")
                .font (.largeTitle)
                .fontWeight(.bold)
                .foregroundStyle(.white)
        }
    }
}

#Preview {
    ContentView()
}
