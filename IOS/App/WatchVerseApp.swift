//
//  WatchVerseApp.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI
import FirebaseCore

@main
struct WatchVerseApp: App {
    
    init() {
        FirebaseApp.configure()
        
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
        journey: .loading,
        universes: []
    )
    
    @State private var navigation = AppNavigation()
    @State private var authentication = AuthenticationService()
    @State private var cloudKit = CloudKitService()
    @State private var contentStore = ContentStore()
    
    var body: some Scene{
        WindowGroup {
            RootView()
                .environment(viewModel)
                .environment(navigation)
                .environment(authentication)
                .environment(cloudKit)
                .environment(contentStore)
                .task {
                    await contentStore.loadInitialContent(
                        universeID: "mcu"
                    )

                    if let universe = contentStore.universe {
                        viewModel.replaceJourney(with: universe)
                    }
                }                .preferredColorScheme(.dark)
                .onAppear {
                    viewModel.authentication = authentication
                    viewModel.cloudKit = cloudKit
                    viewModel.contentStore = contentStore
                }
        }
    }
}
