//
//  MainTabView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct MainTabView: View {
    var body: some View {
        TabView {
            ContentView()
                .tabItem {
                    Label("Journey", systemImage: "map.fill")
                }
        }
    }
}

#Preview {
    MainTabView()
}
