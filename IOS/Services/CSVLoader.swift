//
//  CSVLoader.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-04.
//

import Foundation

struct CSVLoader {
    
    static func loadCSV(named fileName: String) -> String? {

        guard let url = Bundle.main.url(forResource: fileName, withExtension: "csv") else {
            return nil
        }

        return try? String(contentsOf: url, encoding: .utf8)
    }

}
