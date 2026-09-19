from pathlib import Path
import json
import mimetypes

import firebase_admin
from firebase_admin import credentials, firestore, storage


# ---------------------------------------------------------
# CONFIG
# ---------------------------------------------------------

DRY_RUN = False

PROJECT_ID = "watchverse-2e24f"
BUCKET_NAME = "watchverse-2e24f.firebasestorage.app"

key_path = (
    Path.home()
    / "Documents"
    / "WatchVerseSecrets"
    / "watchverse-2e24f-firebase-adminsdk-fbsvc-b1bb59f2c5.json"
)

artwork_folder = (
    Path.home()
    / "Desktop"
    / "watchverse poster"
)

project_root = Path(__file__).resolve().parent.parent
marvel_path = project_root / "Data" / "marvel.json"


# ---------------------------------------------------------
# FIREBASE
# ---------------------------------------------------------

cred = credentials.Certificate(key_path)

firebase_admin.initialize_app(
    cred,
    {
        "projectId": PROJECT_ID,
        "storageBucket": BUCKET_NAME,
    },
)

db = firestore.client()
bucket = storage.bucket()


# ---------------------------------------------------------
# HELPERS
# ---------------------------------------------------------

SUPPORTED_EXTENSIONS = {
    ".png",
    ".jpg",
    ".jpeg",
    ".webp",
}


def build_artwork_index(folder: Path):
    index = {}

    for file_path in folder.iterdir():
        if not file_path.is_file():
            continue

        if file_path.suffix.lower() not in SUPPORTED_EXTENSIONS:
            continue

        key = file_path.stem.lower()

        if key in index:
            print(
                f"⚠️ Duplicate artwork key '{key}': "
                f"{index[key].name} and {file_path.name}"
            )
            continue

        index[key] = file_path

    return index


def find_artwork(index, asset_key: str):
    if not asset_key:
        return None

    return index.get(asset_key.lower())


def upload_artwork(local_file: Path, storage_path: str):
    print(f"⬆️  {local_file.name}")
    print(f"    → {storage_path}")

    if DRY_RUN:
        return

    blob = bucket.blob(storage_path)

    content_type, _ = mimetypes.guess_type(local_file.name)

    blob.upload_from_filename(
        str(local_file),
        content_type=content_type,
    )


# ---------------------------------------------------------
# LOAD LOCAL MCU DATA
# ---------------------------------------------------------

with marvel_path.open("r", encoding="utf-8") as file:
    marvel = json.load(file)

movies = marvel["movies"]

artwork_index = build_artwork_index(artwork_folder)

print()
print(f"Found {len(artwork_index)} artwork files.")
print(f"Found {len(movies)} MCU content items.")
print()

if DRY_RUN:
    print("🧪 DRY RUN — nothing will be uploaded or changed.")
    print()


# ---------------------------------------------------------
# CONTENT POSTERS
# ---------------------------------------------------------

matched_content = 0
missing_content = []

for movie in movies:
    movie_id = movie["id"]
    poster_key = movie.get("poster", "")

    local_file = find_artwork(
        artwork_index,
        poster_key,
    )

    if local_file is None:
        missing_content.append(
            (
                movie_id,
                poster_key,
            )
        )
        continue

    storage_path = (
        f"artwork/mcu/content/"
        f"{movie_id}/"
        f"{local_file.name}"
    )

    upload_artwork(
        local_file,
        storage_path,
    )

    if not DRY_RUN:
        db.collection("universes") \
            .document("mcu") \
            .collection("content") \
            .document(movie_id) \
            .update(
                {
                    "poster": storage_path,
                }
            )

    matched_content += 1


# ---------------------------------------------------------
# MCU UNIVERSE ARTWORK
# ---------------------------------------------------------

print()
print("=== MCU UNIVERSE ARTWORK ===")
print()

universe_ref = (
    db.collection("universes")
    .document("mcu")
)

universe_artwork_fields = {
    "logo": marvel.get("logo"),
    "banner": marvel.get("banner"),
    "poster": marvel.get("poster"),
}

matched_universe = 0
missing_universe = []

for field_name, asset_key in universe_artwork_fields.items():
    if not asset_key:
        continue

    local_file = find_artwork(
        artwork_index,
        asset_key,
    )

    if local_file is None:
        missing_universe.append(
            (
                field_name,
                asset_key,
            )
        )
        continue

    storage_path = (
        f"artwork/mcu/universe/"
        f"{local_file.name}"
    )

    upload_artwork(
        local_file,
        storage_path,
    )

    if not DRY_RUN:
        universe_ref.update(
            {
                field_name: storage_path,
            }
        )

    matched_universe += 1


# ---------------------------------------------------------
# REPORT
# ---------------------------------------------------------

print()
print("========================================")
print("MCU ARTWORK MIGRATION REPORT")
print("========================================")
print()

print(
    f"Content posters matched: "
    f"{matched_content}/{len(movies)}"
)

print(
    f"Universe artwork matched: "
    f"{matched_universe}/{len(universe_artwork_fields)}"
)

if missing_content:
    print()
    print("❌ Missing content artwork:")

    for movie_id, poster_key in missing_content:
        print(
            f"   {movie_id}"
            f" → expected asset '{poster_key}'"
        )

if missing_universe:
    print()
    print("❌ Missing universe artwork:")

    for field_name, asset_key in missing_universe:
        print(
            f"   {field_name}"
            f" → expected asset '{asset_key}'"
        )

print()

if DRY_RUN:
    print(
        "🧪 Dry run complete. "
        "No Firebase data was changed."
    )
else:
    print(
        "✅ Artwork upload and Firestore migration complete."
    )
