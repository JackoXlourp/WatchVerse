import csv
import json

UNIVERSE = {
    "id": "mcu",
    "title": "Marvel",
    "subtitle": "Cinematic Universe",
    "fullTitle": "Marvel Cinematic Universe",
    "description": "The complete Marvel Cinematic Universe in release order.",
    "logo": "hulk2008",
    "banner": "mcu-banner",
    "poster": "placeholder-poster"
}

with open("Data/marvel.csv", newline="", encoding="utf-8") as file:
    reader = csv.DictReader(file)

    movies = list(reader)

converted_movies = []

for movie in movies:

    converted_movies.append({
        "id": movie["ID"],
        "title": movie["Title"],
        "poster": movie["Poster"],
        "year": int(movie["Year"]),
        "runtime": movie["Runtime"],
        "synopsis": movie["Synopsis"],
        "director": movie["Director"],
        "genres": [genre.strip() for genre in movie["Genres"].split(",")],
        "isWatched": movie["isWatched"] == "TRUE",
        "isSkipped": movie["isSkipped"] == "TRUE"
    })
    
universe = UNIVERSE.copy()
universe["movies"] = converted_movies

with open("Data/marvel.json", "w", encoding="utf-8") as file:
    json.dump(universe, file, indent=2)

print("marvel.json created successfully!")
