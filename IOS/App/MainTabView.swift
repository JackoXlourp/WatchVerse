//
//  MainTabView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct MainTabView: View {

    @Environment(AppNavigation.self) private var navigation

    var body: some View {

        @Bindable var navigation = navigation

        TabView(selection: $navigation.selectedTab) {

            NavigationStack {
                HomeView()
            }
            .tag(AppNavigation.Tab.home)
            .tabItem {
                Label("Home", systemImage: "house")
            }

            NavigationStack {
                JourneyView()
            }
            .tag(AppNavigation.Tab.journey)
            .tabItem {
                Label("Journey", systemImage: "map.fill")
            }
            
            NavigationStack {
                BadgeGalleryView()
            }
            .tag(AppNavigation.Tab.badges)
            .tabItem {
                Label("Badges", systemImage: "medal.fill")
            }
        }
    }
}

