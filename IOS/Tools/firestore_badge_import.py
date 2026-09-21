from __future__ import annotations

import json
import re
from pathlib import Path

import firebase_admin
from firebase_admin import credentials, firestore, storage


# ============================================================
# CONFIG
# ============================================================

DRY_RUN = False

PROJECT_ROOT = Path(__file__).resolve().parent.parent

SERVICE_ACCOUNT_PATH = Path.home() / (
    "Documents/WatchVerseSecrets/"
    "watchverse-2e24f-firebase-adminsdk-fbsvc-b1bb59f2c5.json"
)

STORAGE_BUCKET = "watchverse-2e24f.firebasestorage.app"


# ============================================================
# FILE HELPERS
# ============================================================

def find_file(filename: str) -> Path:
    matches = list(PROJECT_ROOT.rglob(filename))

    if not matches:
        raise FileNotFoundError(f"Could not find {filename}")

    if len(matches) > 1:
        print(f"⚠️ Multiple {filename} files found:")
        for match in matches:
            print(f"   {match}")

    return matches[0]


def find_imageset(asset_name: str) -> Path | None:
    target_name = f"{asset_name}.imageset"

    for path in PROJECT_ROOT.rglob(target_name):
        if path.is_dir():
            return path

    return None


def image_file_from_imageset(imageset: Path) -> Path | None:
    contents_path = imageset / "Contents.json"

    if not contents_path.exists():
        return None

    with contents_path.open("r", encoding="utf-8") as file:
        contents = json.load(file)

    for image in contents.get("images", []):
        filename = image.get("filename")

        if filename:
            candidate = imageset / filename

            if candidate.exists():
                return candidate

    return None


# ============================================================
# SWIFT PARSING
# ============================================================

def extract_badge_blocks(source: str) -> list[str]:
    blocks = []
    search_position = 0

    while True:
        start = source.find("Badge(", search_position)

        if start == -1:
            break

        index = start + len("Badge(")
        depth = 1
        in_string = False
        escaped = False

        while index < len(source) and depth > 0:
            char = source[index]

            if in_string:
                if escaped:
                    escaped = False
                elif char == "\\":
                    escaped = True
                elif char == '"':
                    in_string = False
            else:
                if char == '"':
                    in_string = True
                elif char == "(":
                    depth += 1
                elif char == ")":
                    depth -= 1

            index += 1

        if depth != 0:
            raise ValueError("Unbalanced Badge(...) block in BadgeData.swift")

        blocks.append(source[start:index])
        search_position = index

    return blocks


def extract_string(block: str, field: str) -> str:
    match = re.search(
        rf'{re.escape(field)}\s*:\s*"((?:\\.|[^"\\])*)"',
        block,
    )

    if not match:
        raise ValueError(f"Missing string field: {field}")

    value = match.group(1)

    return bytes(value, "utf-8").decode("unicode_escape")


def extract_int(block: str, field: str) -> int:
    match = re.search(
        rf"{re.escape(field)}\s*:\s*(\d+)",
        block,
    )

    if not match:
        raise ValueError(f"Missing integer field: {field}")

    return int(match.group(1))


def extract_string_array(block: str, field: str) -> list[str]:
    match = re.search(
        rf"{re.escape(field)}\s*:\s*\[(.*?)\]",
        block,
        re.DOTALL,
    )

    if not match:
        raise ValueError(f"Missing array field: {field}")

    array_contents = match.group(1)

    return re.findall(
        r'"((?:\\.|[^"\\])*)"',
        array_contents,
    )


def parse_badges() -> list[dict]:
    badge_data_path = find_file("BadgeData.swift")

    print(f"📄 Badge source: {badge_data_path}")

    source = badge_data_path.read_text(encoding="utf-8")

    badge_blocks = extract_badge_blocks(source)

    badges = []

    for block in badge_blocks:
        badge = {
            "id": extract_string(block, "id"),
            "title": extract_string(block, "title"),
            "universeID": extract_string(block, "universeID"),
            "universeTitle": extract_string(block, "universeTitle"),
            "localArtwork": extract_string(block, "artwork"),
            "description": extract_string(block, "description"),
            "requiredContentIDs": extract_string_array(
                block,
                "requiredContentIDs",
            ),
            "sortOrder": extract_int(block, "sortOrder"),
        }

        badges.append(badge)

    return badges


# ============================================================
# FIREBASE
# ============================================================

def initialize_firebase():
    credential = credentials.Certificate(str(SERVICE_ACCOUNT_PATH))

    firebase_admin.initialize_app(
        credential,
        {
            "storageBucket": STORAGE_BUCKET,
        },
    )

    return firestore.client(), storage.bucket()


# ============================================================
# IMPORT
# ============================================================

def main():
    print()
    print("🏅 WatchVerse Badge Firebase Import")
    print("=" * 60)

    if DRY_RUN:
        print("🧪 DRY RUN — Firebase will NOT be modified.")
    else:
        print("🔥 LIVE MODE — Firebase WILL be modified.")

    print()

    badges = parse_badges()

    print(f"Found {len(badges)} badges.")
    print()

    prepared = []

    for badge in badges:
        imageset = find_imageset(badge["localArtwork"])

        if imageset is None:
            print(
                f"❌ {badge['id']}: "
                f"missing asset {badge['localArtwork']}.imageset"
            )
            continue

        image_file = image_file_from_imageset(imageset)

        if image_file is None:
            print(
                f"❌ {badge['id']}: "
                f"no image file found inside {imageset.name}"
            )
            continue

        extension = image_file.suffix.lower()

        remote_artwork = (
            f"artwork/badges/{badge['id']}{extension}"
        )

        firestore_data = {
            "id": badge["id"],
            "title": badge["title"],
            "universeID": badge["universeID"],
            "universeTitle": badge["universeTitle"],
            "artwork": remote_artwork,
            "description": badge["description"],
            "requiredContentIDs": badge["requiredContentIDs"],
            "sortOrder": badge["sortOrder"],
        }

        prepared.append(
            {
                "badge": badge,
                "imageFile": image_file,
                "remoteArtwork": remote_artwork,
                "firestoreData": firestore_data,
            }
        )

        print(f"✅ {badge['id']}")
        print(f"   Local:   {image_file}")
        print(f"   Storage: {remote_artwork}")
        print(
            f"   Required content: "
            f"{len(badge['requiredContentIDs'])}"
        )
        print()

    print("=" * 60)
    print(
        f"Prepared {len(prepared)} / {len(badges)} badges."
    )

    if len(prepared) != len(badges):
        print()
        print("❌ Some badge assets are missing.")
        print("Nothing will be uploaded.")
        return

    if DRY_RUN:
        print()
        print("✅ Dry run complete.")
        print("No Firebase data was changed.")
        return

    db, bucket = initialize_firebase()

    print()
    print("Uploading...")

    for item in prepared:
        badge = item["badge"]
        image_file = item["imageFile"]
        remote_artwork = item["remoteArtwork"]
        firestore_data = item["firestoreData"]

        blob = bucket.blob(remote_artwork)
        blob.upload_from_filename(str(image_file))

        db.collection("badges").document(
            badge["id"]
        ).set(firestore_data)

        print(f"🔥 Uploaded {badge['id']}")

    print()
    print(f"✅ Uploaded {len(prepared)} badges.")


if __name__ == "__main__":
    main()
