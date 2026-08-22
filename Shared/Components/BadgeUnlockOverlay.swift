//
//  Untitled.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-12.
//

import SwiftUI

struct BadgeUnlockOverlay: View {

    let badge: Badge
    let onClose: () -> Void
    let onSeeBadge: () -> Void

    var body: some View {

        ZStack {

            Color.black.opacity(0.35)
                .ignoresSafeArea()

            VStack(spacing: 24) {

                Image(badge.imageName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 160, height: 160)

                Text("Badge Unlocked!")
                    .font(.title.bold())
                    .foregroundStyle(.white)

                Text(badge.title)
                    .font(.title2.bold())
                    .foregroundStyle(Color.watchVerseGold)

                Text(badge.description)
                    .foregroundStyle(.gray)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)

                HStack(spacing: 16) {

                    Button {
                        onClose()
                    } label: {
                        Text("Close")
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(.gray.opacity(0.3))
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                    }

                    Button {
                        onSeeBadge()
                    } label: {
                        Text("See Badge")
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(Color.watchVerseGold)
                            .foregroundStyle(.black)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                    }
                }
                .padding(.horizontal)

            }
            .padding()
            .background {
                RoundedRectangle(cornerRadius: 24)
                    .fill(.ultraThinMaterial)
                    .overlay {
                        RoundedRectangle(cornerRadius: 24)
                            .stroke(Color.white.opacity(0.15), lineWidth: 1)
                    }
            }
            .padding()
        }
    }
}
