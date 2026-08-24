//
//  FilterDropdownView.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-23.
//

import SwiftUI

struct FilterDropdownView: View {
    
    @State private var selectedFilters: Set<String>

    let filters: [String]
    var onApply: (Set<String>) -> Void
    
    init(
        filters: [String],
        selectedFilters: Set<String>,
        onApply: @escaping (Set<String>) -> Void
    ) {
        self.filters = filters
        self._selectedFilters = State(initialValue: selectedFilters)
        self.onApply = onApply
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            
            ForEach(["All"] + filters, id: \.self) { filter in
                Button {
                    if filter == "All" {
                        if selectedFilters.count == filters.count {
                            // Everything selected → clear all
                            selectedFilters.removeAll()
                        } else {
                            // Not everything selected → select everything
                            selectedFilters = Set(filters)
                        }
                    } else {
                        if selectedFilters.contains(filter) {
                            selectedFilters.remove(filter)
                        } else {
                            selectedFilters.insert(filter)
                        }
                    }
                } label: {
                    HStack {
                        Image(systemName:
                                filter == "All"
                                ? (selectedFilters.isEmpty || selectedFilters.count == filters.count ? "checkmark.circle.fill" : "circle")
                                : (selectedFilters.isEmpty || selectedFilters.contains(filter) ? "checkmark.circle.fill" : "circle")
                        )
                        
                        Text(
                            filter
                                .replacingOccurrences(of: "_", with: " ")
                                .capitalized
                        )
                        
                        Spacer()
                    }
                    .foregroundStyle(.white)
                }
            }
            
            Divider()
            
            Button("Apply") {
                onApply(selectedFilters)
            }
            .frame(maxWidth: .infinity)
            .foregroundStyle(Color.watchVerseGold)
        }
        .padding()
        .frame(width: 250)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }
}
