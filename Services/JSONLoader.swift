//
//  JSONLoader.swift
//  WatchVerse
//
//  Created by Maxime Proulx on 2026-07-30.
//

import Foundation

enum JSONLoader {
    
    static func load<T: Decodable>(_ filename: String, as type: T.Type) -> T {
        
        guard let url = Bundle.main.url(forResource: filename, withExtension: "json") else {
            fatalError("Could not find \(filename).json")
        }
        
        do {
            let data = try Data(contentsOf: url)
            return try JSONDecoder().decode(T.self, from:data)
        } catch {
            print(error)
            fatalError("Failed to load \(filename).json")
        }
    }
}
