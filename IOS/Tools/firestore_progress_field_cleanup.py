from pathlib import Path

import firebase_admin
from firebase_admin import credentials, firestore


# ---------------------------------------------------------
# CONFIG
# ---------------------------------------------------------

DRY_RUN = False

PROJECT_ID = "watchverse-2e24f"

key_path = (
    Path.home()
    / "Documents"
    / "WatchVerseSecrets"
    / "watchverse-2e24f-firebase-adminsdk-fbsvc-b1bb59f2c5.json"
)


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
# CLEANUP
# ---------------------------------------------------------

universes = db.collection("universes").stream()

documents_found = 0
documents_to_clean = 0

print()

if DRY_RUN:
    print("🧪 DRY RUN — nothing will be changed.")
    print()


for universe in universes:
    content_docs = (
        db.collection("universes")
        .document(universe.id)
        .collection("content")
        .stream()
    )

    for document in content_docs:
        documents_found += 1

        data = document.to_dict()

        has_watched = "isWatched" in data
        has_skipped = "isSkipped" in data

        if not has_watched and not has_skipped:
            continue

        documents_to_clean += 1

        print(
            f"🧹 {universe.id}/{document.id}"
        )

        if not DRY_RUN:
            document.reference.update(
                {
                    "isWatched": firestore.DELETE_FIELD,
                    "isSkipped": firestore.DELETE_FIELD,
                }
            )


# ---------------------------------------------------------
# REPORT
# ---------------------------------------------------------

print()
print("========================================")
print("PROGRESS FIELD CLEANUP REPORT")
print("========================================")
print()

print(f"Content documents found: {documents_found}")
print(f"Documents needing cleanup: {documents_to_clean}")

print()

if DRY_RUN:
    print("🧪 Dry run complete. Firestore was not changed.")
else:
    print("✅ Firestore progress fields removed.")
