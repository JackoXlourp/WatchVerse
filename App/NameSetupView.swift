//
//  NameSetupView.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-22.
//

import SwiftUI

struct NameSetupView: View {
    
    @Environment(AuthenticationService.self)
    private var authentication

    @Environment(CloudKitService.self)
    private var cloudKit

    @State private var name = ""

    var body: some View {

        ZStack {

            Image("AppBackground")
                .resizable()
                .scaledToFill()
                .frame(
                    width: UIScreen.main.bounds.width,
                    height: UIScreen.main.bounds.height
                )
                .clipped()
                .ignoresSafeArea()

            VStack(spacing: 24) {

                Text("Welcome to WatchVerse")
                    .font(.title)
                    .foregroundStyle(Color.watchVerseGold)

                Text("Choose your Username")
                    .foregroundStyle(.white.opacity(0.7))

                TextField("Name", text: $name)
                    .textFieldStyle(.roundedBorder)
                    .padding(.horizontal)

                Button("Continue") {

                    guard !name.isEmpty else {
                        return
                    }

                    if var user = authentication.currentUser {

                        user.displayName = name

                        authentication.currentUser = user

                        cloudKit.save(user: user)

                        authentication.needsName = false
                    }

                }

            }
            .padding()
        }
    }
}

#Preview {
    NameSetupView()
}
