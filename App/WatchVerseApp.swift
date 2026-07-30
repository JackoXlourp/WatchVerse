//
//  WatchVerseApp.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

@main
struct WatchVerseApp: App {
    
    @State private var viewModel = JourneyViewModel(journey: universes[0])
    
    var body: some Scene{
        WindowGroup {
            RootView()
                .environment(viewModel)
        }
    }
}
