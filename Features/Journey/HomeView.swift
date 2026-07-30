//
//  HomeView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//

import SwiftUI

struct HomeView: View {

    @Environment(JourneyViewModel.self) private var viewModel

    var body: some View {

        NavigationStack {

            ZStack {
                Color.black
                    .ignoresSafeArea()

                ScrollView(showsIndicators: false) {

                    VStack(alignment: .leading, spacing: 32) {

                        Text("WatchVerse")
                            .font(.largeTitle.bold())
                            .foregroundStyle(.white)

                        // MARK: Continue Watching

                        Text("Continue Watching")
                            .font(.headline)
                            .foregroundStyle(.white)

                        UniverseCard(universe: viewModel.journey)

                        // MARK: Your Universes

                        Text("Your Universes")
                            .font(.headline)
                            .foregroundStyle(.white)

                        ForEach(viewModel.universes) { universe in
                            UniverseCard(universe: universe)
                        }
                    }
                    .padding()
                }
            }
        }
    }
}

#Preview {
    HomeView()
        .environment(
            JourneyViewModel(
                journey: universes[0],
                universes: universes
            )
        )
}
