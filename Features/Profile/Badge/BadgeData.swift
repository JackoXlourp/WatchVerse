//
//  BadgeData.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-10.
//

import Foundation

struct BadgeData {

    static let all: [Badge] = [

        Badge(
            id: "founder",
            title: "Founder",
            universe: "WATCHVERSE",
            imageName: "badge_founder",
            description: "A special badge reserved for the first WatchVerse members.",
            isUnlocked: false,
            requiredMovieIDs: []
        ),

        Badge(
            id: "infinity-saga",
            title: "Infinity Saga",
            universe: "MARVEL",
            imageName: "badge_infinity_saga",
            description: "...",
            isUnlocked: false,
            requiredMovieIDs: [
                "iron-man",
                "the-incredible-hulk",
                "iron-man-2",
                "thor",
                "captain-america-the-first-avenger",
                "the-avengers",
                "a-funny-thing-happened-on-the-way-to-thors-hammer",
                "the-consultant",
                "item-47",
                "iron-man-3",
                "thor-the-dark-world",
                "captain-america-the-winter-soldier",
                "guardians-of-the-galaxy",
                "avengers-age-of-ultron",
                "ant-man",
                "all-hail-the-king",
                "agent-carter-one-shot",
                "captain-america-civil-war",
                "doctor-strange",
                "guardians-of-the-galaxy-vol-2",
                "spider-man-homecoming",
                "thor-ragnarok",
                "black-panther",
                "avengers-infinity-war",
                "ant-man-and-the-wasp",
                "captain-marvel",
                "avengers-endgame",
                "spider-man-far-from-home",
                "beta-blocker"
            ]
        ),
        
        Badge(
            id: "multiverse-saga",
            title: "Multiverse Saga",
            universe: "MARVEL",
            imageName: "badge_multiverse_saga",
            description: "...",
            isUnlocked: false,
            requiredMovieIDs: [
                // Phase Four
                "wandavision",
                "the-falcon-and-the-winter-soldier",
                "black-widow",
                "loki-season-1",
                "shang-chi-and-the-legend-of-the-ten-rings",
                "eternals",
                "spider-man-no-way-home",
                "hawkeye",
                "doctor-strange-in-the-multiverse-of-madness",
                "moon-knight",
                "ms-marvel",
                "thor-love-and-thunder",
                "she-hulk-attorney-at-law",
                "black-panther-wakanda-forever",

                // Phase Five
                "echo",
                "ant-man-and-the-wasp-quantumania",
                "guardians-of-the-galaxy-vol-3",
                "secret-invasion",
                "loki-season-2",
                "the-marvels",
                "deadpool-and-wolverine",
                "agatha-all-along",
                "daredevil-born-again-season-1",

                // Phase Five / Six bridge
                "captain-america-brave-new-world",
                "thunderbolts",

                // Phase Six
                "the-fantastic-four-first-steps",
                "daredevil-born-again-season-2",
                "wonder-man",
                "the-punisher-one-last-kill",
                "spider-man-brand-new-day",
                "avengers-doomsday",
                "avengers-secret-wars"
            ]
        )
    ]
}
