//
//  au.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-09.
//

import SwiftUI
import AuthenticationServices

struct AuthenticationView: View {
    
    @Environment(AuthenticationService.self)
    private var authentication

    var body: some View {

        ZStack {

            Image("AppBackground")
                .resizable()
                .scaledToFill()
                .ignoresSafeArea()

            VStack(spacing: 30) {

                Spacer()

                Image("WatchVerseLogo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 120)

                Text("Welcome to WatchVerse")
                    .font(.largeTitle.bold())
                    .foregroundStyle(.white)

                Text("Your cinematic journey deserves to be remembered.")
                    .font(.title3)
                    .foregroundStyle(.gray)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)

                Spacer()

                SignInWithAppleButton(
                    .continue,
                    onRequest: { request in
                        authentication.configure(request)
                    },
                    onCompletion: { result in
                        authentication.handle(result)
                    }
                )
                .signInWithAppleButtonStyle(.white)
                    .frame(maxWidth: 375)
                    .frame(height: 56)
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                Text("Your progress, collections and achievements will stay with you forever.")
                    .font(.footnote)
                    .foregroundStyle(.gray)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)

            }
            .padding()
        }
    }
}
