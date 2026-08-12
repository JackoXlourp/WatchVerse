//
//  SettingsButton.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-03.
//

import SwiftUI

struct SettingsButton: View {

    var body: some View {

        NavigationLink {

            SettingsView()

        } label: {

            Image(systemName: "gearshape")
                .font(.title2)
                .foregroundStyle(.white)
        }
    }
}

#Preview {
    NavigationStack {
        SettingsButton()
    }
}
