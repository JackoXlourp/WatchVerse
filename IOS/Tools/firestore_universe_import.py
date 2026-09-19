from pathlib import Path
import json
import re

import firebase_admin
from firebase_admin import credentials, firestore


# ---------------------------------------------------------
# CONFIG
# ---------------------------------------------------------

DRY_RUN = False

PROJECT_ID = "watchverse-2e24f"

V1_UNIVERSES = {
    "lotr",
    "wizarding-world",
    "jurassic",
}

key_path = (
    Path.home()
    / "Documents"
    / "WatchVerseSecrets"
    / "watchverse-2e24f-firebase-adminsdk-fbsvc-b1bb59f2c5.json"
)

project_root = Path(__file__).resolve().parent.parent
data_folder = project_root / "Data"
catalog_path = data_folder / "universes.json"


# ---------------------------------------------------------
# FIREBASE
# ---------------------------------------------------------

if not firebase_admin._apps:
    cred = credentials.Certificate(key_path)

    firebase_admin.initialize_app(
        cred,
        {
            "projectId": PROJECT_ID,
        },
    )

db = firestore.client()


# ---------------------------------------------------------
# HELPERS
# ---------------------------------------------------------

def content_type_for(item):
    runtime = item.get("runtime", "").lower()

    if "episode" in runtime or "season" in runtime:
        return "series"

    if re.fullmatch(r"\d+\s*m", runtime):
        return "short"

    return "movie"


# ---------------------------------------------------------
# LOAD CATALOG
# ---------------------------------------------------------

with catalog_path.open("r", encoding="utf-8") as file:
    catalog = json.load(file)

selected_catalog = [
    item
    for item in catalog
    if item["id"] in V1_UNIVERSES
]


print()

if DRY_RUN:
    print("🧪 DRY RUN — Firestore will not be changed.")
    print()


# ---------------------------------------------------------
# IMPORT
# ---------------------------------------------------------

universe_count = 0
content_count = 0
errors = []


for sort_order, catalog_item in enumerate(catalog):

    universe_id = catalog_item["id"]

    if universe_id not in V1_UNIVERSES:
        continue

    state = catalog_item["state"]
    file_name = catalog_item["file"]

    universe_path = data_folder / f"{file_name}.json"

    if not universe_path.exists():
        errors.append(
            f"{universe_id}: missing {universe_path.name}"
        )
        continue

    with universe_path.open("r", encoding="utf-8") as file:
        universe = json.load(file)

    # -----------------------------------------------------
    # UNIVERSE DOCUMENT
    # -----------------------------------------------------

    universe_data = {
        "id": universe_id,
        "state": state,
        "title": universe["title"],
        "subtitle": universe["subtitle"],
        "fullTitle": universe["fullTitle"],
        "description": universe["description"],
        "banner": universe["banner"],
        "poster": universe["poster"],
        "filters": universe.get("filters"),
        "sortOrder": sort_order,
        "schemaVersion": 1,
        "contentVersion": 1,
    }

    print()
    print(
        f"🌌 {universe_id} "
        f"[{state}] "
        f"→ {universe['title']}"
    )

    if not DRY_RUN:
        (
            db.collection("universes")
            .document(universe_id)
            .set(
                universe_data,
                merge=True,
            )
        )

    universe_count += 1


    # -----------------------------------------------------
    # CONTENT
    # -----------------------------------------------------

    movies = universe.get("movies", [])

    for index, item in enumerate(movies):

        timeline_order = (index + 1) * 100

        item_type = content_type_for(item)

        content_data = {
            "id": item["id"],
            "title": item["title"],
            "poster": item["poster"],
            "year": item["year"],
            "runtime": item["runtime"],
            "synopsis": item["synopsis"],
            "director": item["director"],
            "genres": item["genres"],
            "tags": item.get("tags", []),
            "releaseStatus": "released",
            "type": item_type,
            "timelineOrder": timeline_order,
        }

        print(
            f"   {timeline_order}: "
            f"{item['id']} "
            f"[{item_type}]"
        )

        if not DRY_RUN:
            (
                db.collection("universes")
                .document(universe_id)
                .collection("content")
                .document(item["id"])
                .set(content_data)
            )

        content_count += 1


# ---------------------------------------------------------
# REPORT
# ---------------------------------------------------------

print()
print("========================================")
print("V1 UNIVERSE IMPORT REPORT")
print("========================================")
print()

print(
    f"Universes matched: "
    f"{universe_count}/{len(V1_UNIVERSES)}"
)

print(
    f"Content items prepared: "
    f"{content_count}"
)

if errors:
    print()
    print("❌ Errors:")

    for error in errors:
        print(f"   {error}")

print()

if DRY_RUN:
    print("🧪 Dry run complete. Firestore was not changed.")
else:
    print("✅ V1 universe data imported into Firestore.")
