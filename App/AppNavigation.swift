//
//  AppNavigation.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-08.
//

import SwiftUI

@Observable
class AppNavigation {

    var selectedTab: Tab = .home

    enum Tab: Hashable {
        case home
        case journey
        case badges
    }
}
