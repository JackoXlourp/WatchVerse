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

project_root = Path(__file__).resolve().parent.parent

assets_folder = (
    project_root
    / "WatchVerse"
    / "Assets.xcassets"
)


# Only artwork we actually have and want remote right now.
ARTWORK = [
    {
        "universe": "mcu",
        "field": "banner",
        "asset": "mcu-banner",
    },
    {
        "universe": "lotr",
        "field": "banner",
        "asset": "lotr-banner",
    },
    {
        "universe": "lotr",
        "field": "poster",
        "asset": "lotr-poster",
    },
]


# ---------------------------------------------------------
# FIREBASE
# ---------------------------------------------------------

if not firebase_admin._apps:
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
# FIND IMAGE INSIDE XCODE IMAGESET
# ---------------------------------------------------------

def find_asset_file(asset_name: str):

    matches = list(
        assets_folder.rglob(
            f"{asset_name}.imageset"
        )
    )

    if not matches:
        return None

    imageset = matches[0]

    contents_path = (
        imageset
        / "Contents.json"
    )

    if not contents_path.exists():
        return None

    with contents_path.open(
        "r",
        encoding="utf-8",
    ) as file:
        contents = json.load(file)

    candidates = []

    for image in contents.get("images", []):
        filename = image.get("filename")

        if not filename:
            continue

        image_path = imageset / filename

        if image_path.exists():
            candidates.append(image_path)

    if not candidates:
        return None

    # If Xcode contains several scales,
    # use the largest actual image file.
    return max(
        candidates,
        key=lambda path: path.stat().st_size,
    )


# ---------------------------------------------------------
# MIGRATE
# ---------------------------------------------------------

matched = 0
missing = []


print()

if DRY_RUN:
    print(
        "🧪 DRY RUN — nothing will be "
        "uploaded or changed."
    )
    print()


for item in ARTWORK:

    universe_id = item["universe"]
    field = item["field"]
    asset_name = item["asset"]

    local_file = find_asset_file(
        asset_name
    )

    if local_file is None:
        missing.append(asset_name)
        continue

    storage_path = (
        f"artwork/{universe_id}/universe/"
        f"{asset_name}{local_file.suffix.lower()}"
    )

    print(
        f"🖼️ {asset_name}"
    )

    print(
        f"   {local_file.name}"
    )

    print(
        f"   → {storage_path}"
    )

    if not DRY_RUN:

        blob = bucket.blob(
            storage_path
        )

        content_type, _ = (
            mimetypes.guess_type(
                local_file.name
            )
        )

        blob.upload_from_filename(
            str(local_file),
            content_type=content_type,
        )

        (
            db.collection("universes")
            .document(universe_id)
            .update(
                {
                    field: storage_path,
                }
            )
        )

    matched += 1


# ---------------------------------------------------------
# REPORT
# ---------------------------------------------------------

print()
print(
    "========================================"
)
print(
    "UNIVERSE ARTWORK MIGRATION REPORT"
)
print(
    "========================================"
)
print()

print(
    f"Artwork matched: "
    f"{matched}/{len(ARTWORK)}"
)

if missing:

    print()
    print("❌ Missing artwork:")

    for asset in missing:
        print(f"   {asset}")

print()

if DRY_RUN:
    print(
        "🧪 Dry run complete. "
        "Firebase was not changed."
    )
else:
    print(
        "✅ Universe artwork migration complete."
    )
