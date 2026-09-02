//
//  BadgeData.swift
//  WatchVerse
//
//  Created by Maxime on 2026-08-10.
//

import Foundation

struct BadgeData {

    static let all: [Badge] = [
        //MARK: Founder
        Badge(
            id: "founder",
            title: "Founder",
            universe: "WATCHVERSE",
            imageName: "badge_founder",
            description: "A special badge reserved for the first WatchVerse members.",
            isUnlocked: false,
            requiredMovieIDs: []
        ),
        //MARK: MARVEL
        Badge(
            id: "infinity-saga",
            title: "Infinity Saga",
            universe: "MARVEL",
            imageName: "badge_infinity_saga",
            description: "Experience the beginning of the Marvel Cinematic Universe, from Iron Man to the epic battle against Thanos.",
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
                "black-widow",
                "doctor-strange",
                "guardians-of-the-galaxy-vol-2",
                "spider-man-homecoming",
                "thor-ragnarok",
                "black-panther",
                "avengers-infinity-war",
                "ant-man-and-the-wasp",
                "captain-marvel",
                "avengers-endgame"
            ]
        ),
        
        Badge(
            id: "multiverse-saga",
            title: "Multiverse Saga",
            universe: "MARVEL",
            imageName: "badge_multiverse_saga",
            description: "Explore the expanding Marvel Universe as heroes face new threats across realities, timelines, and dimensions.",
            isUnlocked: false,
            requiredMovieIDs: [
                // Phase Four
                "wandavision",
                "the-falcon-and-the-winter-soldier",
                "spider-man-far-from-home",
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
        ),
        
        //MARK: LOTR
        Badge(
            id: "second-age",
            title: "The Second Age",
            universe: "LORD OF THE RINGS",
            imageName: "badge_second_age",
            description: "Experience the rise of Sauron and the forging of the Rings of Power.",
            isUnlocked: false,
            requiredMovieIDs: [
                "the-rings-of-power"
            ]
        ),

        Badge(
            id: "the-hobbits-journey",
            title: "The Hobbit's Journey",
            universe: "LORD OF THE RINGS",
            imageName: "badge_hobbits_journey",
            description: "Follow Bilbo Baggins' adventure from the Shire to Erebor.",
            isUnlocked: false,
            requiredMovieIDs: [
                "an-unexpected-journey",
                "the-desolation-of-smaug",
                "the-battle-of-the-five-armies"
            ]
        ),

        Badge(
            id: "war-of-the-rohirrim",
            title: "The War of the Rohirrim",
            universe: "LORD OF THE RINGS",
            imageName: "badge_war_of_the_rohirrim",
            description: "Discover the legend of Helm Hammerhand and the origins of Helm's Deep.",
            isUnlocked: false,
            requiredMovieIDs: [
                "the-war-of-the-rohirrim"
            ]
        ),
        Badge(
            id: "one-ring-bearer",
            title: "One Ring Bearer",
            universe: "LORD OF THE RINGS",
            imageName: "badge_one_ring_bearer",
            description: "Complete Frodo's journey to Mount Doom.",
            isUnlocked: false,
            requiredMovieIDs: [
                "the-fellowship-of-the-ring",
                "the-two-towers",
                "the-return-of-the-king"
            ]
        ),

        Badge(
            id: "middle-earth-explorer",
            title: "Middle-earth Explorer",
            universe: "LORD OF THE RINGS",
            imageName: "badge_middle_earth_explorer",
            description: "Complete the entire released Middle-earth saga.",
            isUnlocked: false,
            requiredMovieIDs: [
                "the-rings-of-power",
                "an-unexpected-journey",
                "the-desolation-of-smaug",
                "the-battle-of-the-five-armies",
                "the-war-of-the-rohirrim",
                "the-fellowship-of-the-ring",
                "the-two-towers",
                "the-return-of-the-king"
            ]
        ),
        
        Badge(
            id: "jurassic-park-legacy",
            title: "Jurassic Park Legacy",
            universe: "JURASSIC",
            imageName: "badge_jurassic_park_legacy",
            description: "Return to the beginning of the dinosaur era and experience the original Jurassic Park trilogy.",
            isUnlocked: false,
            requiredMovieIDs: [
                "jurassic-park",
                "the-lost-world-jurassic-park",
                "jurassic-park-iii"
            ]
        ),

        Badge(
            id: "jurassic-world-era",
            title: "Jurassic World Era",
            universe: "JURASSIC",
            imageName: "badge_jurassic_world_era",
            description: "Witness the rise, fall, and rebirth of a new dinosaur age.",
            isUnlocked: false,
            requiredMovieIDs: [
                "jurassic-world",
                "fallen-kingdom",
                "jurassic-world-dominion",
                "jurassic-world-rebirth"
            ]
        ),

        Badge(
            id: "jurassic-survival-stories",
            title: "Jurassic Survival Stories",
            universe: "JURASSIC",
            imageName: "badge_jurassic_survival_stories",
            description: "Follow the survivors who face a world where dinosaurs have returned to the wild.",
            isUnlocked: false,
            requiredMovieIDs: [
                "camp-cretaceous",
                "battle-at-big-rock",
                "chaos-theory"
            ]
        ),
        
        //MARK: FAST & FURIOUOS
        Badge(
            id: "street-racing-origins",
            title: "Street Racing Origins",
            universe: "FAST & FURIOUS",
            imageName: "badge_street_racing_origins",
            description: "Experience the beginning of the Fast Saga and the underground racing world that started it all.",
            isUnlocked: false,
            requiredMovieIDs: [
                "the-fast-and-the-furious",
                "2-fast-2-furious",
                "turbo-charged-prelude",
                "tokyo-drift"
            ]
        ),

        Badge(
            id: "the-fast-family",
            title: "The Fast Family",
            universe: "FAST & FURIOUS",
            imageName: "badge_the_fast_family",
            description: "Witness Dom, Brian, and the crew come together to become a family.",
            isUnlocked: false,
            requiredMovieIDs: [
                "los-bandoleros",
                "fast-and-furious-4",
                "fast-five",
                "fast-and-furious-6"
            ]
        ),

        Badge(
            id: "global-missions",
            title: "Global Missions",
            universe: "FAST & FURIOUS",
            imageName: "badge_global_missions",
            description: "Follow the crew as their missions become bigger, more dangerous, and impossible.",
            isUnlocked: false,
            requiredMovieIDs: [
                "furious-7",
                "the-fate-of-the-furious",
                "f9",
                "fast-x"
            ]
        ),

        Badge(
            id: "beyond-the-family",
            title: "Beyond the Family",
            universe: "FAST & FURIOUS",
            imageName: "badge_beyond_the_family",
            description: "Explore the expanded Fast universe beyond Dom's main crew.",
            isUnlocked: false,
            requiredMovieIDs: [
                "spy-racers",
                "hobbs-and-shaw"
            ]
        )
    ]
    
    static func badgesContaining(movieID: String) -> [Badge] {
        all.filter { badge in
            badge.requiredMovieIDs.contains(movieID)
        }
    }
}


