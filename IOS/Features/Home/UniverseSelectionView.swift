//
//  UniverseSelectionView.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-22.
//

import SwiftUI

struct UniverseSelectionView: View {

    @Environment(JourneyViewModel.self)
    private var viewModel

    @Environment(AuthenticationService.self)
    private var authentication

    @Environment(ContentStore.self)
    private var contentStore

    var body: some View {

        ZStack {

            Image("AppBackground")
                .resizable()
                .scaledToFill()
                .ignoresSafeArea()

            ScrollView(showsIndicators: false) {

                VStack(spacing: 28) {

                    Spacer()
                        .frame(height: 70)

                    VStack(spacing: 12) {

                        Text("Choose Your Universe")
                            .font(.largeTitle.bold())
                            .foregroundStyle(Color.watchVerseGold)
                            .multilineTextAlignment(.center)

                        Text("Choose the universe you want to explore next.")
                            .font(.subheadline)
                            .foregroundStyle(.white.opacity(0.75))
                            .multilineTextAlignment(.center)

                        HStack(spacing: 8) {
                            Rectangle()
                                .fill(Color.watchVerseGold.opacity(0.45))
                                .frame(width: 36, height: 1)

                            Image(systemName: "sparkles")
                                .font(.caption)
                                .foregroundStyle(Color.watchVerseGold)

                            Rectangle()
                                .fill(Color.watchVerseGold.opacity(0.45))
                                .frame(width: 36, height: 1)
                        }
                        .padding(.top, 4)
                    }
                    .padding(.horizontal)

                    LazyVGrid(
                        columns: [
                            GridItem(.flexible()),
                            GridItem(.flexible())
                        ],
                        spacing: 20
                    ) {

                        ForEach(contentStore.availableUniverses) { universe in

                            UniverseCard(
                                universe: universe,
                                onTap: {

                                    Task {

                                        await contentStore.preloadArtwork(
                                            for: universe
                                        )

                                        if var user = authentication.currentUser {

                                            user.settings.currentUniverseID =
                                                universe.id

                                            authentication.currentUser = user

                                            await authentication
                                                .saveCurrentUserToFirestore()
                                        }

                                        viewModel.replaceJourney(
                                            with: universe
                                        )
                                    }
                                }
                            )
                        }
                    }
                    
                    Text("You can change universes anytime.")
                        .font(.caption)
                        .foregroundStyle(.white.opacity(0.55))
                        .multilineTextAlignment(.center)
                        .padding(.top, 4)
                    
                }
                .padding()
                .padding(.bottom, 80)
            }
        }
    }
}
