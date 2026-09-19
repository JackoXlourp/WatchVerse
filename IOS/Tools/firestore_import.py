from pathlib import Path

import firebase_admin
from firebase_admin import credentials, firestore

key_path = (
    Path.home()
    / "Documents"
    / "WatchVerseSecrets"
    / "watchverse-2e24f-firebase-adminsdk-fbsvc-b1bb59f2c5.json"
)

cred = credentials.Certificate(key_path)
firebase_admin.initialize_app(cred)

db = firestore.client()

print("Firestore connection ready")

import json

project_root = Path(__file__).resolve().parent.parent
marvel_path = project_root / "Data" / "marvel.json"

with marvel_path.open("r", encoding="utf-8") as file:
    marvel = json.load(file)

movies = marvel.pop("movies")

universe_data = {
    **marvel,
    "state": "available",
    "sortOrder": 100,
    "schemaVersion": 1,
    "contentVersion": 1,
}

universe_ref = db.collection("universes").document("mcu")
universe_ref.set(universe_data)

for index, movie in enumerate(movies, start=1):
    movie.pop("isWatched", None)
    movie.pop("isSkipped", None)

    movie["timelineOrder"] = index * 100

    special_ids = {
    "werewolf-by-night",
    "the-guardians-of-the-galaxy-holiday-special",
}

    if "Episode" in movie.get("runtime", ""):
        movie["type"] = "series"
    elif "Short" in movie.get("genres", []):
        movie["type"] = "short"
    elif movie["id"] in special_ids:
        movie["type"] = "special"
    else:
        movie["type"] = "movie"

    universe_ref.collection("content").document(movie["id"]).set(movie)

actual_count = sum(1 for _ in universe_ref.collection("content").stream())
print(f"Firestore contains {actual_count} MCU content items")
