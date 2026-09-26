//
//  RootView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-26.
//

import SwiftUI

struct RootView: View {
    
    @Environment(JourneyViewModel.self)
    private var viewModel
    
    @Environment(AuthenticationService.self)
    private var authentication
    
    @Environment(AppNavigation.self)
    private var navigation
    
    @Environment(ContentStore.self)
    private var contentStore
    
    @State private var showingBadgeOverlay = false
    @State private var popupBadge: Badge?
    
    @AppStorage("notifyNewUniverses")
    private var notifyNewUniverses = false
    
    var body: some View {
        
        Group {
            
            if authentication.isLoading || contentStore.isLoading {
                
                splashScreenView()
                
            } else if authentication.needsName {
                
                NameSetupView()
                
            } else if authentication.isSignedIn {

                if authentication.currentUser?
                    .settings.currentUniverseID == nil {

                    UniverseSelectionView()

                } else {

                    MainTabView()
                }

            } else {
                
                AuthenticationView()
                
            }
            
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .task {
            authentication.restoreSession()
        }
        .onChange(of: authentication.isLoading) { _, isLoading in

            guard !isLoading else {
                return
            }

            Task {

                if let universeID =
                    authentication.currentUser?
                        .settings.currentUniverseID {

                    await contentStore.loadInitialContent(
                        universeID: universeID
                    )

                    if let universe = contentStore.universe {
                        viewModel.replaceJourney(
                            with: universe
                        )
                    }

                } else {

                    await contentStore
                        .loadCatalogWithoutSelectedUniverse()
                }
            }
        }
        .onChange(of: authentication.isSignedIn) { _, signedIn in
            
            guard signedIn,
                  authentication.currentUser != nil,
                  let firebaseUID = authentication.firebaseUID else {
                return
            }
            
            Task {
                
                do {

                    if authentication.existingFirestoreProfile != nil,
                       let firestoreUser = authentication.currentUser {

                        await MainActor.run {
                            viewModel.loadWatchedMovies(
                                from: firestoreUser
                            )
                        }

                        return
                    }
                    
                    let loadedFromFirestore =
                    try await authentication.loadFirestoreUser(
                        uid: firebaseUID
                    )
                    
                    if loadedFromFirestore {
                        
                        if let firestoreUser = authentication.currentUser {
                            
                            await MainActor.run {
                                viewModel.loadWatchedMovies(
                                    from: firestoreUser
                                )
                            }
                        }
                        
                        return
                    }
                    
                    let legacyUserExists =
                    try await authentication
                        .loadLegacyCloudKitUserIfPresent()
                    
                    if legacyUserExists,
                       let legacyUser = authentication.currentUser {
                        
                        await MainActor.run {
                            
                            viewModel.loadWatchedMovies(
                                from: legacyUser
                            )
                            
                            if legacyUser.isFounder,
                               !legacyUser.shownBadgePopups.contains("founder"),
                               let badge = contentStore.badges.first(
                                where: { $0.id == "founder" }
                               ) {
                                
                                viewModel.pendingBadgePopup = badge
                            }
                        }
                        
                        await authentication.migrateLegacyUserIfNeeded(
                            notifyNewUniverses: notifyNewUniverses
                        )
                        
                    } else {
                        
                        await MainActor.run {
                            authentication.needsName = true
                        }
                    }
                }   catch {
                    
                    print(
                        "Firestore user load failed:",
                        error.localizedDescription
                    )
                }
            }
        }
        .onChange(of: authentication.needsName) { _, needsName in
            
            guard !needsName,
                  let user = authentication.currentUser else {
                return
            }
            
            if user.isFounder,
               !user.shownBadgePopups.contains("founder"),
               let badge = contentStore.badges.first(where: { $0.id == "founder" }) {
                
                viewModel.pendingBadgePopup = badge
            }
        }
        .onChange(of: viewModel.pendingBadgePopup) { _, badge in
            
            guard let badge else {
                return
            }
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                
                popupBadge = badge
                showingBadgeOverlay = true
                
                if var user = authentication.currentUser {
                    
                    if !user.shownBadgePopups.contains(badge.id) {
                        user.shownBadgePopups.append(badge.id)
                        
                        authentication.currentUser = user
                        
                        Task {
                            await authentication.saveCurrentUserToFirestore(
                                notifyNewUniverses: notifyNewUniverses
                            )
                        }
                    }
                }
                
                viewModel.pendingBadgePopup = nil
            }
        }
        .overlay {
            if showingBadgeOverlay,
               let badge = popupBadge {
                
                BadgeUnlockOverlay(
                    badge: badge,
                    onClose: {
                        showingBadgeOverlay = false
                    },
                    onSeeBadge: {
                        showingBadgeOverlay = false
                        navigation.openBadge(id: badge.id)
                    }
                )
            }
        }
    }
}
