//
//  SettingsButton.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-03.
//

import SwiftUI

struct SettingsButton: View {

    @State private var showSettings = false

    var body: some View {

        Button {
            showSettings = true
        } label: {
            Image(systemName: "gearshape")
                .font(.title2)
                .foregroundStyle(.white)
        }
        .sheet(isPresented: $showSettings) {
            SettingsView()
        }
    }
}

#Preview {
    NavigationStack {
        SettingsButton()
    }
}
