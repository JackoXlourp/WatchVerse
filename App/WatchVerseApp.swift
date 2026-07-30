//
//  WatchVerseApp.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

@main
struct WatchVerseApp: App {
    
    @State private var viewModel = JourneyViewModel(journey: marvelUniverse)
    
    var body: some Scene{
        WindowGroup {
            RootView()
                .environment(viewModel)
        }
    }
}
