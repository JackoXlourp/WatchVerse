//
//  splashScreenView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-08-02.
//

import SwiftUI

struct splashScreenView: View {

    var body: some View {

        Image("wv-launch")
            .resizable()
            .scaledToFill()
            .ignoresSafeArea()
    }
}

#Preview {
    splashScreenView()
}
