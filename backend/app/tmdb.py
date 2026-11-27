from functools import lru_cache

import httpx

from app.config import settings


@lru_cache(maxsize=512)
def metadata(tmdb_id: int | None) -> dict:
    empty = {"overview": "", "poster_url": "", "backdrop_url": "", "cast": []}
    if tmdb_id is None or not settings.tmdb_api_key:
        return empty
    try:
        response = httpx.get(f"https://api.themoviedb.org/3/movie/{tmdb_id}",
                             params={"api_key": settings.tmdb_api_key, "append_to_response": "credits"}, timeout=8)
        response.raise_for_status()
        data = response.json()
        cast = [{"name": person.get("name", ""), "character": person.get("character", "")}
                for person in data.get("credits", {}).get("cast", [])[:8]]
        image_base = "https://image.tmdb.org/t/p/w500"
        return {"overview": data.get("overview", ""),
                "poster_url": image_base + data["poster_path"] if data.get("poster_path") else "",
                "backdrop_url": image_base + data["backdrop_path"] if data.get("backdrop_path") else "",
                "release_date": data.get("release_date", ""), "runtime": data.get("runtime") or 0, "cast": cast}
    except (httpx.HTTPError, ValueError):
        return empty


def details(movie: dict) -> dict:
    return {"movie": movie, **metadata(movie.get("tmdb_id"))}
