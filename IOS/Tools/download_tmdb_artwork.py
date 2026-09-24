import os
import requests

TMDB_TOKEN = os.environ.get("TMDB_TOKEN")

if not TMDB_TOKEN:
    raise SystemExit("Missing TMDB_TOKEN environment variable.")

HEADERS = {
    "Authorization": f"Bearer {TMDB_TOKEN}",
    "accept": "application/json",
}

OUTPUT_DIR = "lotr_artwork"
os.makedirs(OUTPUT_DIR, exist_ok=True)

CONTENT = {
    "an-unexpected-journey": ("movie", "The Hobbit: An Unexpected Journey"),
    "the-battle-of-the-five-armies": ("movie", "The Hobbit: The Battle of the Five Armies"),
    "the-desolation-of-smaug": ("movie", "The Hobbit: The Desolation of Smaug"),
    "the-rings-of-power": ("tv", "The Lord of the Rings: The Rings of Power"),
    "the-war-of-the-rohirrim": ("movie", "The Lord of the Rings: The War of the Rohirrim"),
}

def search_content(content_type, title):
    response = requests.get(
        f"https://api.themoviedb.org/3/search/{content_type}",
        headers=HEADERS,
        params={
            "query": title,
            "include_adult": "false",
            "language": "en-US",
            "page": 1,
        },
        timeout=30,
    )
    response.raise_for_status()

    results = response.json().get("results", [])

    if not results:
        return None

    return results[0]

def download_poster(slug, content_type, title):
    item = search_content(content_type, title)

    if not item:
        print(f"❌ No TMDB result: {title}")
        return

    poster_path = item.get("poster_path")

    if not poster_path:
        print(f"❌ No poster_path: {title}")
        return

    image_url = f"https://image.tmdb.org/t/p/original{poster_path}"

    image_response = requests.get(image_url, timeout=60)
    image_response.raise_for_status()

    extension = os.path.splitext(poster_path)[1] or ".jpg"
    output_path = os.path.join(
        OUTPUT_DIR,
        f"{slug}{extension}"
    )

    with open(output_path, "wb") as file:
        file.write(image_response.content)

    print(
        f"✅ {title}\n"
        f"   TMDB ID: {item['id']}\n"
        f"   poster_path: {poster_path}\n"
        f"   saved: {output_path}\n"
    )

for slug, (content_type, title) in CONTENT.items():
    download_poster(slug, content_type, title)
