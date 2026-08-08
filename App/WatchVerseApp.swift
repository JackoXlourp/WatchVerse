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
            .foregroundColor: UIColor(
                red: 0.85,
                green: 0.72,
                blue: 0.45,
                alpha: 1
            )
        ]

        appearance.titleTextAttributes = [
            .foregroundColor: UIColor(
                red: 0.85,
                green: 0.72,
                blue: 0.45,
                alpha: 1
            )
        ]

        UINavigationBar.appearance().standardAppearance = appearance
        UINavigationBar.appearance().scrollEdgeAppearance = appearance
    }
    
    @State private var viewModel = JourneyViewModel(
        journey: universes.first!,
        universes: universes
    )
    
    var body: some Scene{
        WindowGroup {
            splashScreenView()
                .environment(viewModel)
        }
    }
}
