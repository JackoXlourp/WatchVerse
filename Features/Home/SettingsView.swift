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
        
        NavigationStack {
            
            ZStack {
                
                Color.black
                    .ignoresSafeArea()
                
                Text("Settings")
                    .font(.largeTitle)
                    .foregroundStyle(.white)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                
                ToolbarItem(placement: .topBarLeading) {
                    
                    Button {
                        
                        dismiss()
                        
                    } label: {
                        
                        Image(systemName: "chevrin.left")
                    }
                }
            }
        }
        
    }
}

#Preview {
    SettingsView()
}
