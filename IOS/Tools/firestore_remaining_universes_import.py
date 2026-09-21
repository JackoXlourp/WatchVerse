from pathlib import Path
import json
import re

import firebase_admin
from firebase_admin import credentials, firestore


# ---------------------------------------------------------
# CONFIG
# ---------------------------------------------------------

DRY_RUN = True

PROJECT_ID = "watchverse-2e24f"

# These are the universes already migrated by the existing importers.
MIGRATED_UNIVERSE_IDS = {
    "mcu",
    "lotr",
    "wizarding-world",
    "jurassic",
}

EXPECTED_REMAINING_UNIVERSE_COUNT = 6

PROGRESS_FIELDS = {"isWatched", "isSkipped"}
REQUIRED_UNIVERSE_FIELDS = {
    "id",
    "title",
    "subtitle",
    "fullTitle",
    "description",
    "banner",
    "poster",
}
REQUIRED_CONTENT_FIELDS = {
    "id",
    "title",
    "poster",
    "year",
    "runtime",
    "synopsis",
    "director",
    "genres",
}
VALID_RELEASE_STATUSES = {"released", "comingSoon"}

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
    credential = credentials.Certificate(key_path)
    firebase_admin.initialize_app(
        credential,
        {
            "projectId": PROJECT_ID,
        },
    )

db = firestore.client()


# ---------------------------------------------------------
# HELPERS
# ---------------------------------------------------------

def content_type_for(item):
    runtime = item["runtime"].lower()

    if "episode" in runtime or "season" in runtime:
        return "series"

    if re.fullmatch(r"\d+\s*m", runtime):
        return "short"

    return "movie"


def missing_fields(data, required_fields):
    return sorted(
        field
        for field in required_fields
        if field not in data or data[field] is None
    )


def validate_universe(universe, catalog_item, source_path):
    errors = []
    warnings = []

    missing = missing_fields(universe, REQUIRED_UNIVERSE_FIELDS)

    if missing:
        errors.append(
            f"{source_path.name}: missing universe fields: {', '.join(missing)}"
        )

    if universe.get("id") != catalog_item["id"]:
        errors.append(
            f"{source_path.name}: source id {universe.get('id')!r} does not match "
            f"catalog id {catalog_item['id']!r}"
        )

    movies = universe.get("movies")

    if not isinstance(movies, list):
        errors.append(f"{source_path.name}: movies must be an array")
        return errors, warnings

    seen_ids = set()
    progress_field_item_count = 0

    for position, item in enumerate(movies, start=1):
        if not isinstance(item, dict):
            errors.append(
                f"{source_path.name}: item {position} is not an object"
            )
            continue

        missing = missing_fields(item, REQUIRED_CONTENT_FIELDS)

        if missing:
            errors.append(
                f"{source_path.name}: item {position} missing content fields: "
                f"{', '.join(missing)}"
            )
            continue

        content_id = item["id"]

        if content_id in seen_ids:
            errors.append(
                f"{source_path.name}: duplicate content id {content_id!r}"
            )

        seen_ids.add(content_id)

        if not isinstance(item["genres"], list):
            errors.append(
                f"{source_path.name}: {content_id} genres must be an array"
            )

        if "tags" in item and not isinstance(item["tags"], list):
            errors.append(
                f"{source_path.name}: {content_id} tags must be an array"
            )

        release_status = item.get("releaseStatus", "released")

        if release_status not in VALID_RELEASE_STATUSES:
            errors.append(
                f"{source_path.name}: {content_id} has invalid releaseStatus "
                f"{release_status!r}"
            )

        source_progress_fields = sorted(PROGRESS_FIELDS.intersection(item))

        if source_progress_fields:
            progress_field_item_count += 1

    if progress_field_item_count:
        warnings.append(
            f"{source_path.name}: {progress_field_item_count} content items "
            "contain legacy progress fields; they will not be written"
        )

    return errors, warnings


def content_document(item, index):
    document = {
        "id": item["id"],
        "title": item["title"],
        "type": content_type_for(item),
        "timelineOrder": (index + 1) * 100,
        "poster": item["poster"],
        "year": item["year"],
        "runtime": item["runtime"],
        "synopsis": item["synopsis"],
        "director": item["director"],
        "genres": item["genres"],
        "tags": item.get("tags", []),
        "releaseStatus": item.get("releaseStatus", "released"),
    }

    unexpected_progress_fields = PROGRESS_FIELDS.intersection(document)

    if unexpected_progress_fields:
        raise ValueError(
            "Progress fields would be written: "
            f"{', '.join(sorted(unexpected_progress_fields))}"
        )

    return document


def universe_document(universe, catalog_item, sort_order):
    return {
        "id": catalog_item["id"],
        "state": catalog_item["state"],
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


# ---------------------------------------------------------
# LOAD AND VALIDATE SOURCES
# ---------------------------------------------------------

with catalog_path.open("r", encoding="utf-8") as file:
    catalog = json.load(file)

remaining_catalog = [
    item
    for item in catalog
    if item["id"] not in MIGRATED_UNIVERSE_IDS
]

errors = []
warnings = []
prepared_universes = []

if len(remaining_catalog) != EXPECTED_REMAINING_UNIVERSE_COUNT:
    errors.append(
        "Expected "
        f"{EXPECTED_REMAINING_UNIVERSE_COUNT} remaining universes, found "
        f"{len(remaining_catalog)} in universes.json"
    )

for sort_order, catalog_item in enumerate(catalog):
    if catalog_item["id"] in MIGRATED_UNIVERSE_IDS:
        continue

    source_path = data_folder / f"{catalog_item['file']}.json"

    if not source_path.exists():
        errors.append(
            f"{catalog_item['id']}: missing source file {source_path.name}"
        )
        continue

    with source_path.open("r", encoding="utf-8") as file:
        universe = json.load(file)

    source_errors, source_warnings = validate_universe(
        universe,
        catalog_item,
        source_path,
    )
    errors.extend(source_errors)
    warnings.extend(source_warnings)

    prepared_universes.append(
        {
            "catalog": catalog_item,
            "sourcePath": source_path,
            "sortOrder": sort_order,
            "universe": universe,
        }
    )


# ---------------------------------------------------------
# REPORT AND IMPORT
# ---------------------------------------------------------

print()
print("========================================")
print("REMAINING UNIVERSE IMPORT")
print("========================================")
print()
print("Mode: DRY RUN (no Firebase writes)" if DRY_RUN else "Mode: LIVE IMPORT")
print()

total_content_count = 0
existing_universe_ids = []
existing_content_paths = []

for prepared in prepared_universes:
    catalog_item = prepared["catalog"]
    universe = prepared["universe"]
    universe_id = catalog_item["id"]
    movies = universe["movies"]
    universe_ref = db.collection("universes").document(universe_id)
    universe_exists = universe_ref.get().exists
    existing_content_ids = {
        document.id
        for document in universe_ref.collection("content").stream()
    }

    if universe_exists:
        existing_universe_ids.append(universe_id)

    print(
        f"{universe_id}: {len(movies)} content items "
        f"→ universes/{universe_id} "
        f"({'EXISTS' if universe_exists else 'new'})"
    )

    if not DRY_RUN:
        universe_ref.set(
            universe_document(
                universe,
                catalog_item,
                prepared["sortOrder"],
            ),
            merge=True,
        )

    for index, item in enumerate(movies):
        content_data = content_document(item, index)
        content_path = (
            f"universes/{universe_id}/content/{content_data['id']}"
        )

        if content_data["id"] in existing_content_ids:
            existing_content_paths.append(content_path)

        print(
            f"  {content_data['timelineOrder']}: {content_path} "
            f"[{content_data['type']}]"
        )

        if not DRY_RUN:
            universe_ref.collection("content").document(
                content_data["id"]
            ).set(content_data)

    total_content_count += len(movies)

print()
print("========================================")
print("REPORT")
print("========================================")
print(f"Universes prepared: {len(prepared_universes)}")
print(f"Content items prepared: {total_content_count}")
print(
    "Existing universe documents: "
    f"{len(existing_universe_ids)}"
)
print(
    "Existing content documents: "
    f"{len(existing_content_paths)}"
)

if existing_universe_ids:
    print("Existing universe IDs: " + ", ".join(existing_universe_ids))

if existing_content_paths:
    print("Existing content paths:")
    for path in existing_content_paths:
        print(f"  {path}")

if warnings:
    print(f"Warnings: {len(warnings)}")
    for warning in warnings:
        print(f"  WARNING: {warning}")

if errors:
    print(f"Errors: {len(errors)}")
    for error in errors:
        print(f"  ERROR: {error}")
    raise SystemExit(1)

print("Validation: passed")
print(
    "Progress-field validation: passed "
    "(isWatched and isSkipped are never written)"
)
print("Firebase writes: none" if DRY_RUN else "Firebase import: complete")
