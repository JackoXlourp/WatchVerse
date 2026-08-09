//
//  WatchVerseApp.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

@main
struct WatchVerseApp: App {

    init() {
        let appearance = UINavigationBarAppearance()
        appearance.configureWithTransparentBackground()

        appearance.largeTitleTextAttributes = [
            .foregroundColor: UIColor.watchVerseGold
        ]

        appearance.titleTextAttributes = [
            .foregroundColor: UIColor.watchVerseGold
        ]

        UINavigationBar.appearance().standardAppearance = appearance
        UINavigationBar.appearance().scrollEdgeAppearance = appearance
    }
    
    @State private var viewModel = JourneyViewModel(
        journey: universes.first!,
        universes: universes
    )
    
    @State private var navigation = AppNavigation()
    
    var body: some Scene{
        WindowGroup {
            splashScreenView()
                .environment(viewModel)
                .environment(navigation)
        }
    }
}
