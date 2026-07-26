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
            
            Text("Journey")
                .font(.largeTitle)
                .foregroundStyle(.white)
        }
    }
}

#Preview {
    JourneyView()
}
