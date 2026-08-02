//
//  WatchVerseApp.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

@main
struct WatchVerseApp: App {
    
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
