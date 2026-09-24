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
    
    @UIApplicationDelegateAdaptor(AppDelegate.self)
    private var appDelegate
    
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
    @State private var contentStore = ContentStore()
    
    var body: some Scene{
        WindowGroup {
            RootView()
                .environment(viewModel)
                .environment(navigation)
                .environment(authentication)
                .environment(contentStore)
                .preferredColorScheme(.dark)
                .onAppear {
                    viewModel.authentication = authentication
                    viewModel.contentStore = contentStore
                    
                    appDelegate.requestNotificationPermission()
                }
        }
    }
}
