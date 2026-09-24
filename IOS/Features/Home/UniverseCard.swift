import SwiftUI

struct UniverseCard: View {

    let universe: Universe
    var isLocked = false
    var isCurrent = false
    var onTap: (() -> Void)? = nil

    var body: some View {

        if let onTap, !isLocked {
            Button(action: onTap) {
                cardContent
            }
            .buttonStyle(.plain)
        } else {
            cardContent
        }
    }

    private var cardContent: some View {

        VStack(alignment: .leading, spacing: 16) {

            Color.clear
                .aspectRatio(0.68, contentMode: .fit)
                .frame(maxWidth: .infinity)
                .overlay {
                    ArtworkImageView(
                        source: universe.poster,
                        placeholder: "placeholder-poster"
                    )
                    .scaledToFill()
                }
                .saturation(isLocked ? 0 : 1)
                .clipped()
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(
                            Color.watchVerseGold.opacity(0.8),
                            lineWidth: 1
                        )
                )
                .overlay {
                    if isLocked {
                        Image(systemName: "lock.fill")
                            .font(.system(size: 36, weight: .bold))
                            .foregroundStyle(.white)
                            .padding(10)
                            .background(.ultraThinMaterial)
                            .clipShape(Circle())
                            .overlay(
                                Circle()
                                    .stroke(
                                        Color.white.opacity(0.15),
                                        lineWidth: 1
                                    )
                            )
                    }
                }
            
            HStack(spacing: 12) {

                Text(universe.title)
                    .font(.headline)
                    .foregroundStyle(Color.watchVerseGold)
                    .lineLimit(2)
                    .frame(height: 44, alignment: .top)

                Spacer()
            }
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color.white.opacity(0.05))
        .overlay(
            RoundedRectangle(cornerRadius: 24)
                .stroke(
                    Color.watchVerseGold
                        .opacity(isCurrent ? 0.9 : 0.35),
                    lineWidth: isCurrent ? 2 : 1
                )
        )
        .overlay(alignment: .topTrailing) {
            if isCurrent {
                Text("CURRENT")
                    .font(.caption2.bold())
                    .tracking(0.8)
                    .foregroundStyle(.black)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(Color.watchVerseGold)
                    .clipShape(Capsule())
                    .padding(10)
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(
            color: .black.opacity(0.25),
            radius: 8,
            x: 0,
            y: 4
        )
    }
}

#Preview {
    let previewUniverse = Universe(
        id: "preview",
        title: "Marvel",
        subtitle: "The Infinity Saga",
        fullTitle: "Marvel Cinematic Universe",
        description: "",
        banner: "placeholder-poster",
        poster: "placeholder-poster",
        filters: nil,
        movies: []
    )

    ZStack {
        Color.black
            .ignoresSafeArea()

        UniverseCard(
            universe: previewUniverse,
            onTap: {}
        )
        .padding()
    }
}
