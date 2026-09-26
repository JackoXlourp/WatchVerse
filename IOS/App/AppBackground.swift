//
//  AppBackground.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-25.
//

import SwiftUI

struct AppBackground: View {

    var body: some View {

        GeometryReader { geometry in

            Image("AppBackground")
                .resizable()
                .scaledToFill()
                .frame(width: geometry.size.width)
                .clipped()
        }
        .accessibilityHidden(true)
    }
}

private struct AppBackgroundModifier: ViewModifier {

    func body(content: Content) -> some View {
        content
            .background {
                AppBackground()
                    .ignoresSafeArea()
            }
    }
}

extension View {

    func appBackground() -> some View {
        modifier(AppBackgroundModifier())
    }
}
