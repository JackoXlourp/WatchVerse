//
//  SettingsView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-08-02.
//

import SwiftUI

struct SettingsView: View {
    
    @Environment(\.dismiss) private var dismiss
    
    @Environment(JourneyViewModel.self)
    private var viewModel
    
    @Environment(AuthenticationService.self)
    private var authentication
    
    @Environment(CloudKitService.self)
    private var cloudKit
    
    @AppStorage("notifyNewUniverses")
    private var notifyNewUniverses = false
    
    @State
    private var showResetJourneyAlert = false
    
    @State
    private var showLogoutAlert = false
    
    @State
    private var showDeleteAccountAlert = false
    
    @State
    private var showCloudKitResetAlert = false
    
    private let appVersion =
    Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Uknown"
    
    var body: some View {
        
        NavigationStack {
            
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
                
                ScrollView(showsIndicators: false) {
                    
                    VStack(alignment: .leading, spacing: 32) {
                        
                        Color.clear
                            .frame(height: 70)
                        
                        Text("Settings")
                            .font(.largeTitle.bold())
                            .foregroundStyle(.white)
                        
                        //MARK: WATCHING
                        settingsSection(title: "WATCHING") {
                            
                            Toggle(
                                isOn: Binding(
                                    get: {
                                        authentication.currentUser?.settings.showReleaseYears ?? true
                                    },
                                    set: { newValue in
                                        
                                        authentication.currentUser?.settings.showReleaseYears = newValue
                                        
                                        if let user = authentication.currentUser {
                                            cloudKit.save(user: user)
                                        }
                                    }
                                )
                            ) { Label {
                                
                                Text("Show Release Years")
                                    .foregroundStyle(.white)
                            } icon: {
                                
                                Image(systemName: "calendar")
                                    .foregroundStyle(.white.opacity(0.85))
                                    .frame(width: 24)
                            }
                            }
                            .tint(.watchVerseGold)
                            .padding()
                            
                            Divider()
                                .overlay(.white.opacity(0.08))
                            
                            HStack {
                                
                                Label("Spoiler Protection",systemImage: "lock.fill")
                                    .foregroundStyle(.white)
                                
                                Spacer()
                                
                                Text("Coming Soon")
                                    .font(.caption)
                                    .foregroundStyle(.gray)
                            }
                            .padding()
                        }
                        
                        // MARK: NOTIFICATIONS
                        settingsSection(title: "NOTIFICATIONS") {
                            
                            Toggle(isOn: $notifyNewUniverses) {
                                
                                Label {
                                    
                                    Text("Notify About New Universes")
                                        .foregroundStyle(.white)
                                } icon: {
                                    
                                    Image(systemName: "bell")
                                        .foregroundStyle(.white.opacity(0.85))
                                        .frame(width: 24)
                                }
                            }
                            .tint(.watchVerseGold)
                            .padding()
                            
                            Divider()
                                .overlay(.white.opacity(0.08))
                            
                            HStack {
                                
                                Label {
                                    
                                    Text("New Episode Alerts")
                                        .foregroundStyle(.white)
                                } icon: {
                                    
                                    Image(systemName: "tv")
                                        .foregroundStyle(.white.opacity(0.85))
                                        .frame(width: 24)
                                }
                                
                                Spacer()
                                
                                Text("Coming Soon")
                                    .font(.caption)
                                    .foregroundStyle(.gray)
                            }
                            .padding()
                        }
                        
                        //MARK: ACCOUNT
                        settingsSection(title: "ACCOUNT") {
                            
                            HStack {

                                Image(systemName: "person.circle.fill")
                                    .font(.title2)
                                    .frame(width: 24)

                                VStack(alignment: .leading, spacing: 4) {
                                    
                                    Text(authentication.currentUser?.displayName ?? "")
                                        .font(.headline)

                                    if let user = authentication.currentUser {

                                        if user.isFounder {

                                            Text("WatchVerse Founder | \(user.joinedDate.formatted(.dateTime.month(.wide).year()))")
                                                .font(.caption)
                                                .foregroundStyle(.secondary)

                                        } else {

                                            Text("Joined \(user.joinedDate.formatted(.dateTime.month(.wide).year()))")
                                                .font(.caption)
                                                .foregroundStyle(.secondary)

                                        }
                                    }

                                }

                                Spacer()
                            }
                            .padding()
                            
                            Divider()
                                .overlay(.white.opacity(0.08))
                            
                            
                            Button {
                                
                                showLogoutAlert = true
                                
                            } label: {
                                
                                HStack {
                                    
                                    Label {
                                        
                                        Text("Log Out")
                                            .foregroundStyle(.red)
                                        
                                    } icon: {
                                        
                                        Image(systemName: "rectangle.portrait.and.arrow.right")
                                            .foregroundStyle(.red)
                                            .frame(width: 24)
                                    }
                                    
                                    Spacer()
                                }
                                .padding()
                            }
                            
                            Divider()
                                .overlay(.white.opacity(0.08))

                            Button {

                                showDeleteAccountAlert = true

                            } label: {

                                HStack {

                                    Label {

                                        Text("Delete Account")
                                            .foregroundStyle(.red)

                                    } icon: {

                                        Image(systemName: "person.crop.circle.badge.xmark")
                                            .foregroundStyle(.red)
                                            .frame(width: 24)

                                    }

                                    Spacer()
                                }
                                .padding()
                            }
                        }
                        
                        //MARK: DATA
                        settingsSection(title: "DATA") {
                            
                            Button {
                                
                                showResetJourneyAlert = true
                                
                            } label: {
                                
                                HStack {
                                    
                                    Label {
                                        
                                        Text("Reset Journey Progress")
                                            .foregroundStyle(.white)
                                        
                                    } icon: {
                                        
                                        Image(systemName: "arrow.counterclockwise")
                                            .foregroundStyle(.white.opacity(0.85))
                                            .frame(width: 24)
                                    }
                                    
                                    Spacer()
                                    
                                }
                                .padding()
                            }
                            
                            Divider()
                                .overlay(.white.opacity(0.08))
                            
                            HStack {
                                
                                Label {
                                    
                                    Text("Reset All Progress")
                                        .foregroundStyle(.red)
                                    
                                } icon: {
                                    
                                    Image(systemName: "trash")
                                        .foregroundStyle(.red)
                                        .frame(width: 24)
                                }
                                
                                Spacer()
                                
                            }
                            .padding()
                        }
                            
                           
                            
                            //MARK: ABOUT
                            settingsSection(title: "ABOUT") {
                                
                                HStack {
                                    
                                    Label {
                                        
                                        Text("Version")
                                            .foregroundStyle(.white)
                                        
                                    } icon: {
                                        
                                        Image(systemName: "info.circle")
                                            .foregroundStyle(.white.opacity(0.85))
                                            .frame(width: 24)
                                    }
                                    
                                    Spacer()
                                    
                                    Text(appVersion)
                                        .foregroundStyle(.gray)
                                }
                                .padding()
                            }
                        }
                        .padding()
                        .padding(.bottom, 140)
                    }
                }
            }
            .alert("Reset Current Journey Progress?", isPresented: $showResetJourneyAlert) {
                
                Button("Cancel", role: .cancel) {}
                
                Button("Reset", role: .destructive) {
                    
                    viewModel.resetJourney()
                    
                }
            } message: {
                
                Text("This will mark every movie in your current journey as unwatched.")
            }
            
            .alert("Log Out?", isPresented: $showLogoutAlert) {
                
                Button("Cancel", role: .cancel) {
                    
                }
                
                Button("Log Out", role: .destructive) {
                    authentication.logout()
                }
                
            } message: {
                
                Text("You will need to sign in again to access your WatchVerse account.")
                
            }
        
            .alert("Delete Account?", isPresented: $showDeleteAccountAlert) {

                Button("Cancel", role: .cancel) {
                    
                }

                Button("Delete", role: .destructive) {

                    authentication.deleteAccount(cloudKit: cloudKit)

                }

            } message: {

                Text("This will permanently delete your WatchVerse account, progress, badges, and settings.")

            }
            
        }
    }
    
    @ViewBuilder
    private func settingsSection<Content: View>(
        title: String,
        @ViewBuilder content: () -> Content
    ) -> some View {
        
        VStack(alignment: .leading, spacing: 12) {
            
            Text(title)
                .font(.headline)
                .foregroundStyle(.white)
            
            VStack(spacing: 0) {
                content()
            }
            .background(Color.white.opacity(0.05))
            .clipShape(RoundedRectangle(cornerRadius: 18))
        }
    }


#Preview {
    SettingsView()
}
