//
//  SettingsView.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-08-02.
//

import SwiftUI

struct SettingsView: View {
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        @ViewBuilder
        private func settingsSection(
            title: String,
            rows: [String]
        ) -> some View {
            
            VStack(alignment: .leading, spacing: 12) {
                
                Text(title)
                    .font(.headline)
                    .foregroundStyle(.white)
                
                VStack(spacing: 0) {
                    
                    ForEach(rows, id: \.self) { row in
                        
                        HStack {
                            
                            Text(row)
                                .foregroundStyle(.white)
                            
                            Spacer()
                        }
                        .padding()
                        
                        if row != rows.last {
                            
                            Divider()
                                .overlay(.white.opacity(0.1))
                        }
                    }
                }
                .background(Color.white.opacity(0.05))
                .clipShape(RoundedRectangle(cornerRadius: 18))
            }
        }
        
        NavigationStack {
            
            ZStack {
                
                Color.black
                    .ignoresSafeArea()
                
                ScrollView(showsIndicators: false) {
                    
                    VStack(alignment: .leading, spacing: 32) {
                        
                        Text("Settings")
                            .font(.largeTitle.bold())
                            .foregroundStyle(.white)
                        
                        settingsSection(
                            title: "Watching",
                            rows: [
                                "Coming Soon"
                            ]
                        )
                        
                        settingsSection(
                            title: "Notifications",
                            rows: [
                                "Coming Soon"
                            ]
                        )
                        
                        settingsSection(
                            title: "Data",
                            rows: [
                                "Reset Journey Progress"
                            ]
                        )
                        
                        settingsSection(
                            title: "About",
                            rows: [
                                "Version 1.0"
                            ]
                        )
                    }
                    .padding()
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                
                ToolbarItem(placement: .topBarLeading) {
                    
                    Button {
                        
                        dismiss()
                        
                    } label: {
                        
                        Image(systemName: "chevron.left")
                    }
                }
            }
        }
        
    }
}

#Preview {
    SettingsView()
}
