//
//  ArtworkImageView.swift
//  WatchVerse
//
//  Created by Maxime on 2026-09-18.
//

import SwiftUI
import UIKit
import FirebaseStorage

enum ArtworkImageCache {
    static let shared = NSCache<NSString, UIImage>()
}

enum ArtworkDiskCache {
    static let folderURL: URL = {
        let cachesDirectory = FileManager.default.urls(
            for: .cachesDirectory,
            in: .userDomainMask
        )[0]

        let folder = cachesDirectory
            .appendingPathComponent("ArtworkCache", isDirectory: true)

        try? FileManager.default.createDirectory(
            at: folder,
            withIntermediateDirectories: true
        )

        return folder
    }()
    
    static func fileURL(for source: String) -> URL {
        folderURL.appendingPathComponent(source)
    }
    
    static func image(for source: String) -> UIImage? {
        let url = fileURL(for: source)

        guard
            let data = try? Data(contentsOf: url),
            let image = UIImage(data: data)
        else {
            return nil
        }

        return image
    }
    
    static func save(_ data: Data, for source: String) {
        let url = fileURL(for: source)

        try? FileManager.default.createDirectory(
            at: url.deletingLastPathComponent(),
            withIntermediateDirectories: true
        )

        try? data.write(
            to: url,
            options: .atomic
        )
    }
}

enum ArtworkPreloader {
    static func preload(_ source: String) async {
        guard source.hasPrefix("artwork/") else {
            return
        }

        if ArtworkImageCache.shared.object(
            forKey: source as NSString
        ) != nil {
            return
        }

        if let diskImage = ArtworkDiskCache.image(for: source) {
            ArtworkImageCache.shared.setObject(
                diskImage,
                forKey: source as NSString
            )
            return
        }

        do {
            let url = try await Storage.storage()
                .reference(withPath: source)
                .downloadURL()

            let (data, _) = try await URLSession.shared.data(from: url)

            guard let image = UIImage(data: data) else {
                throw URLError(.cannotDecodeContentData)
            }

            ArtworkDiskCache.save(
                data,
                for: source
            )

            ArtworkImageCache.shared.setObject(
                image,
                forKey: source as NSString
            )

        } catch {
            guard !Task.isCancelled else {
                return
            }

            print(
                "❌ Artwork preload failed for \(source):",
                error.localizedDescription
            )
        }
    }
}

struct ArtworkImageView: View {
    let source: String
    let placeholder: String
    
    init(source: String, placeholder: String) {
        self.source = source
        self.placeholder = placeholder

        let cachedImage =
            ArtworkImageCache.shared.object(
                forKey: source as NSString
            )
            ?? ArtworkDiskCache.image(for: source)

        if let cachedImage {
            ArtworkImageCache.shared.setObject(
                cachedImage,
                forKey: source as NSString
            )
        }

        _remoteImage = State(
            initialValue: cachedImage
        )
    }

    @State private var remoteURL: URL?
    @State private var remoteImage: UIImage?

    private var isRemoteStoragePath: Bool {
        source.hasPrefix("artwork/")
    }

    var body: some View {
        Group {
            if isRemoteStoragePath {
                if let remoteImage {
                    Image(uiImage: remoteImage)
                        .resizable()

                } else if let remoteURL {
                    AsyncImage(url: remoteURL) { phase in
                        switch phase {
                        case .empty:
                            Image(placeholder)
                                .resizable()

                        case .success(let image):
                            image
                                .resizable()

                        case .failure:
                            Image(placeholder)
                                .resizable()

                        @unknown default:
                            Image(placeholder)
                                .resizable()
                        }
                    }
                } else {
                    Image(placeholder)
                        .resizable()
                }
            } else {
                let localName = UIImage(named: source) != nil
                    ? source
                    : placeholder

                Image(localName)
                    .resizable()
            }
        }
        .task(id: source) {
            if let cachedImage = ArtworkImageCache.shared.object(
                forKey: source as NSString
            ) {
                remoteImage = cachedImage
                return
            }

            if let diskImage = ArtworkDiskCache.image(for: source) {
                ArtworkImageCache.shared.setObject(
                    diskImage,
                    forKey: source as NSString
                )

                remoteImage = diskImage
                return
            }

            guard isRemoteStoragePath else {
                return
            }

            await ArtworkPreloader.preload(source)

            guard !Task.isCancelled else {
                return
            }

            remoteImage =
                ArtworkImageCache.shared.object(
                    forKey: source as NSString
                )
                ?? ArtworkDiskCache.image(for: source)
        }
    }
}
