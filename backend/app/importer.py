import csv
from collections import Counter
from pathlib import Path

from app.config import settings
from app.database import connection


def _read_csv(directory: Path, filename: str) -> list[dict[str, str]]:
    path = directory / filename
    if not path.is_file():
        raise RuntimeError(f"Falta {filename} en {directory}")
    with path.open("r", encoding="utf-8-sig", newline="") as source:
        return list(csv.DictReader(source))


def import_movielens() -> dict[str, int]:
    directory = settings.dataset_dir.resolve()
    with connection() as conn:
        if conn.execute("SELECT count(*) AS count FROM movies").fetchone()["count"]:
            return {"movies": 0, "users": 0, "ratings": 0, "skipped": 1}

        source_movies = {row["movieId"]: row for row in _read_csv(directory, "movies.csv")}
        source_links = {row["movieId"]: row for row in _read_csv(directory, "links.csv")}
        ratings = _read_csv(directory, "ratings.csv")
        rating_counts = Counter(row["movieId"] for row in ratings)
        popular_ids = {
            movie_id for movie_id, _ in sorted(rating_counts.items(), key=lambda item: (-item[1], int(item[0])))
            [:settings.top_movies_limit]
        }
        selected = [row for row in ratings if row["movieId"] in popular_ids]
        user_counts = Counter(row["userId"] for row in selected)
        active_users = {user for user, count in user_counts.items() if count >= settings.min_user_ratings}

        movie_rows = []
        genre_rows = []
        for movie_id in popular_ids:
            movie = source_movies.get(movie_id)
            if not movie:
                continue
            links = source_links.get(movie_id, {})
            movie_rows.append((int(movie_id), movie["title"], int(links["tmdbId"]) if links.get("tmdbId") else None, links.get("imdbId") or None))
            if movie.get("genres") and movie["genres"] != "(no genres listed)":
                genre_rows.extend((int(movie_id), genre) for genre in movie["genres"].split("|"))

        conn.executemany("INSERT INTO movies(movie_id,title,tmdb_id,imdb_id) VALUES (%s,%s,%s,%s)", movie_rows)
        conn.executemany("INSERT INTO movie_genres(movie_id,genre) VALUES (%s,%s) ON CONFLICT DO NOTHING", genre_rows)
        conn.executemany("INSERT INTO dataset_users(user_id) VALUES (%s) ON CONFLICT DO NOTHING", [(int(user),) for user in active_users])
        dataset_ratings = [
            (int(row["userId"]), int(row["movieId"]), float(row["rating"]), int(row["timestamp"]))
            for row in selected if row["userId"] in active_users
        ]
        conn.executemany("INSERT INTO dataset_ratings(user_id,movie_id,rating,rated_at) VALUES (%s,%s,%s,%s)", dataset_ratings)
        tag_rows = [
            (int(row["userId"]), int(row["movieId"]), row["tag"], int(row["timestamp"]))
            for row in _read_csv(directory, "tags.csv")
            if row["userId"] in active_users and row["movieId"] in popular_ids
        ]
        conn.executemany("INSERT INTO dataset_tags(user_id,movie_id,tag,tagged_at) VALUES (%s,%s,%s,%s)", tag_rows)
    return {"movies": len(movie_rows), "users": len(active_users), "ratings": len(dataset_ratings), "tags": len(tag_rows)}
