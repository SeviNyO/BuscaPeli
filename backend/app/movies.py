import re
import unicodedata

from app.database import fetch_all, fetch_one

GENRES_ES = {
    "Action": "Acción", "Adventure": "Aventura", "Animation": "Animación", "Comedy": "Comedia",
    "Crime": "Crimen", "Documentary": "Documental", "Drama": "Drama", "Children": "Familia",
    "Fantasy": "Fantasía", "Film-Noir": "Cine negro", "Horror": "Terror", "Musical": "Música",
    "Mystery": "Misterio", "Romance": "Romance", "Sci-Fi": "Ciencia ficción", "Thriller": "Suspenso",
    "War": "Guerra", "Western": "Western", "IMAX": "IMAX",
}
def normalize(value: str) -> str:
    decomposed = unicodedata.normalize("NFD", value.casefold())
    return " ".join("".join(char for char in decomposed if unicodedata.category(char) != "Mn").split())


GENRES_EN = {normalize(value): key for key, value in GENRES_ES.items()}
GENRES_EN.update({normalize(key): key for key in GENRES_ES})
GENRES_EN["todos"] = ""


def movie_row(row: dict) -> dict:
    title = row["title"]
    match = re.search(r"\((\d{4})\)$", title)
    genres = row.get("genres", [])
    result = {
        "movie_id": row["movie_id"], "title": title, "year": int(match.group(1)) if match else None,
        "genres": list(genres) if isinstance(genres, (list, tuple)) else [],
        "tmdb_id": row.get("tmdb_id"), "imdb_id": row.get("imdb_id"),
        "average_rating": float(row.get("average_rating") or 0),
        "rating_count": int(row.get("rating_count") or 0), "poster_url": None,
    }
    if row.get("user_rating") is not None:
        result["user_rating"] = float(row["user_rating"])
    return result


def all_movies() -> list[dict]:
    rows = fetch_all("""
        SELECT m.movie_id,m.title,m.tmdb_id,m.imdb_id,COALESCE(AVG(r.rating),0) AS average_rating,
               COUNT(DISTINCT r.user_id) AS rating_count,COALESCE(array_agg(DISTINCT g.genre)
               FILTER (WHERE g.genre IS NOT NULL),'{}') AS genres
        FROM movies m LEFT JOIN dataset_ratings r ON r.movie_id=m.movie_id
        LEFT JOIN movie_genres g ON g.movie_id=m.movie_id
        GROUP BY m.movie_id,m.title,m.tmdb_id,m.imdb_id
    """)
    return [movie_row(row) for row in rows]


def get_movie(movie_id: int) -> dict | None:
    row = fetch_one("""
        SELECT m.movie_id,m.title,m.tmdb_id,m.imdb_id,COALESCE(AVG(r.rating),0) AS average_rating,
               COUNT(DISTINCT r.user_id) AS rating_count,COALESCE(array_agg(DISTINCT g.genre)
               FILTER (WHERE g.genre IS NOT NULL),'{}') AS genres
        FROM movies m LEFT JOIN dataset_ratings r ON r.movie_id=m.movie_id
        LEFT JOIN movie_genres g ON g.movie_id=m.movie_id WHERE m.movie_id=%s
        GROUP BY m.movie_id,m.title,m.tmdb_id,m.imdb_id
    """, (movie_id,))
    return movie_row(row) if row else None


def levenshtein(first: str, second: str) -> int:
    previous = list(range(len(second) + 1))
    for i, char_a in enumerate(first, 1):
        current = [i]
        for j, char_b in enumerate(second, 1):
            current.append(min(current[-1] + 1, previous[j] + 1, previous[j - 1] + (char_a != char_b)))
        previous = current
    return previous[-1]


def search_movies(search=None, genre=None, sort="title", page=1, page_size=20, min_ratings=1, year=None):
    movies = all_movies()
    selected_genre = GENRES_EN.get(normalize(genre or ""), genre or "")
    results = [movie for movie in movies if
               (not selected_genre or any(normalize(g) == normalize(selected_genre) for g in movie["genres"])) and
               (year is None or movie["year"] == year)]
    query = normalize(search or "")
    direct = [movie for movie in results if not query or query in normalize(movie["title"])]
    suggested = False
    if query and not direct and len(query) >= 4:
        threshold = max(1, len(query) // 5)
        close = [(levenshtein(query, normalize(re.sub(r"\s*\(\d{4}\)$", "", m["title"]))), m) for m in results]
        direct = [movie for distance, movie in sorted(close, key=lambda pair: (pair[0], pair[1]["title"].casefold())) if distance <= threshold][:10]
        suggested = bool(direct)
    if sort == "popular":
        direct.sort(key=lambda movie: (-movie["rating_count"], movie["title"].casefold()))
    elif sort == "top_rated":
        direct = [movie for movie in direct if movie["rating_count"] >= min_ratings]
        direct.sort(key=lambda movie: (-movie["average_rating"], -movie["rating_count"], movie["title"].casefold()))
    else:
        direct.sort(key=lambda movie: movie["title"].casefold())
    start = (page - 1) * page_size
    return {"items": direct[start:start + page_size], "page": page, "page_size": page_size,
            "total": len(direct), "suggested": suggested}
