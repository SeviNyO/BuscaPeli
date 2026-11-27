from contextlib import asynccontextmanager
from uuid import UUID, uuid4

from fastapi import Depends, FastAPI, HTTPException, Query, Request, status
from fastapi.middleware.cors import CORSMiddleware
from starlette.middleware.sessions import SessionMiddleware

from app.config import settings
from app.database import connection, fetch_all, fetch_one, initialize_schema
from app.importer import import_movielens
from app.movies import get_movie, search_movies
from app.recommendations import RecommendationEngine
from app.schemas import Credentials, RatingInput, Registration, TagInput
from app.security import current_user_id, csrf_token, password_hash, password_matches, validate_csrf
from app.tmdb import details as tmdb_details

engine = RecommendationEngine()


@asynccontextmanager
async def lifespan(_: FastAPI):
    initialize_schema()
    imported = import_movielens()
    if imported.get("skipped"):
        print("MovieLens ya importado; se omite la carga CSV.")
    else:
        print(f"MovieLens importado: {imported}")
    engine.precompute(settings.min_common_movies, settings.target_communities, settings.min_recommendation_rating)
    print(f"Grafos listos; {len(set(engine.community_by_user.values()))} comunidades globales.")
    yield


app = FastAPI(title="BuscaPeli API", version="2.0.0", lifespan=lifespan)
app.add_middleware(SessionMiddleware, secret_key=settings.session_secret, session_cookie="JSESSIONID",
                   same_site="lax", https_only=settings.cookie_secure, max_age=60 * 60 * 24 * 14)
app.add_middleware(CORSMiddleware, allow_origins=["http://localhost:5173", "http://127.0.0.1:5173"],
                   allow_credentials=True, allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
                   allow_headers=["Content-Type", "X-XSRF-TOKEN"])


@app.get("/api/v1/health")
def health():
    counts = fetch_one("SELECT (SELECT count(*) FROM movies) AS movies, (SELECT count(*) FROM dataset_ratings) AS ratings")
    return {"status": "ok", **counts}


@app.get("/api/v1/auth/csrf")
def get_csrf(request: Request):
    return {"token": csrf_token(request)}


@app.post("/api/v1/auth/register")
def register(body: Registration, request: Request, _: None = Depends(validate_csrf)):
    user_id = uuid4()
    try:
        with connection() as conn:
            conn.execute("INSERT INTO app_users(user_id,email,display_name,password_hash) VALUES (%s,%s,%s,%s)",
                         (user_id, body.email, body.name, password_hash(body.password)))
    except Exception as error:
        if getattr(error, "sqlstate", None) == "23505":
            raise HTTPException(status_code=409, detail="Ese correo ya está registrado") from error
        raise
    csrf = request.session.get("csrf_token")
    request.session.clear()
    request.session.update({"user_id": str(user_id), "display_name": body.name})
    if csrf:
        request.session["csrf_token"] = csrf
    return {"id": str(user_id), "email": body.email, "name": body.name}


@app.post("/api/v1/auth/login")
def login(body: Credentials, request: Request, _: None = Depends(validate_csrf)):
    user = fetch_one("SELECT user_id,email,display_name,password_hash FROM app_users WHERE email=%s", (body.email,))
    if not user or not password_matches(body.password, user["password_hash"]):
        raise HTTPException(status_code=401, detail="Correo o contraseña incorrectos")
    csrf = request.session.get("csrf_token")
    request.session.clear()
    request.session.update({"user_id": str(user["user_id"]), "display_name": user["display_name"]})
    if csrf:
        request.session["csrf_token"] = csrf
    return {"id": str(user["user_id"]), "email": user["email"], "name": user["display_name"]}


@app.post("/api/v1/auth/logout")
def logout(request: Request, _: None = Depends(validate_csrf)):
    csrf = request.session.get("csrf_token")
    request.session.clear()
    if csrf:
        request.session["csrf_token"] = csrf
    return {"status": "ok"}


@app.get("/api/v1/auth/me")
def session_info(request: Request):
    user_id = request.session.get("user_id")
    if not user_id:
        return {"authenticated": False}
    user = fetch_one("SELECT email,display_name FROM app_users WHERE user_id=%s", (UUID(user_id),))
    if not user:
        request.session.clear()
        return {"authenticated": False}
    return {"authenticated": True, "id": user_id, "name": user["display_name"], "email": user["email"]}


@app.get("/api/v1/movies")
def movies(search: str | None = None, genre: str | None = None, sort: str = "title",
           page: int = Query(1, ge=1), pageSize: int = Query(20, ge=1, le=100),
           minRatings: int = Query(1, ge=1), year: int | None = None):
    if sort not in {"title", "popular", "top_rated"}:
        raise HTTPException(status_code=400, detail="sort inválido")
    return search_movies(search, genre, sort, page, pageSize, minRatings, year)


@app.get("/api/v1/movies/popular")
def popular(limit: int = Query(20, ge=1, le=100)):
    return search_movies(sort="popular", page_size=limit)


@app.get("/api/v1/movies/top-rated")
def top_rated(limit: int = Query(20, ge=1, le=100), minRatings: int = Query(20, ge=1)):
    return search_movies(sort="top_rated", page_size=limit, min_ratings=minRatings)


@app.get("/api/v1/movies/{movie_id}/details")
def movie_details(movie_id: int):
    movie = get_movie(movie_id)
    if not movie:
        raise HTTPException(status_code=404, detail="Película no encontrada")
    return tmdb_details(movie)


@app.get("/api/v1/movies/{movie_id}/poster")
def movie_poster(movie_id: int):
    movie = get_movie(movie_id)
    if not movie:
        raise HTTPException(status_code=404, detail="Película no encontrada")
    if movie["tmdb_id"] is None:
        raise HTTPException(status_code=404, detail="La película no tiene tmdbId asociado")
    data = tmdb_details(movie)
    return {"movie_id": movie_id, "tmdb_id": movie["tmdb_id"], "poster_url": data.get("poster_url", "")}


@app.get("/api/v1/movies/{movie_id}")
def movie_by_id(movie_id: int):
    movie = get_movie(movie_id)
    if not movie:
        raise HTTPException(status_code=404, detail="Película no encontrada")
    return movie


@app.get("/api/v1/recommendations/movies/{movie_id}/by-genre")
def recommendations_by_genre(movie_id: int, limit: int = Query(12, ge=0, le=100)):
    result = engine.by_genre(movie_id, limit)
    if result is None:
        raise HTTPException(status_code=404, detail="Película no encontrada")
    return result


@app.get("/api/v1/recommendations/movies/{movie_id}")
def recommendations(movie_id: int, limit: int = Query(12, ge=0, le=100), minRating: float = Query(4.0, ge=0.5, le=5)):
    result = engine.collaborative(movie_id, limit, minRating)
    if result is None:
        raise HTTPException(status_code=404, detail="Película no encontrada")
    return result


@app.get("/api/v1/recommendations/me")
def my_recommendations(limit: int = Query(20, ge=0, le=100), user_id: UUID = Depends(current_user_id)):
    return engine.personalized(user_id, limit)


def _user_movie_rows(table: str, user_id: UUID, extra_rating=False):
    if table not in {"user_ratings", "favorites", "watchlist"}:
        raise ValueError("Tabla de lista no permitida")
    source = "user_ratings ur" if table == "user_ratings" else f"{table} f"
    owner = "ur.user_id" if table == "user_ratings" else "f.user_id"
    sort_time = "ur.updated_at" if table == "user_ratings" else "f.created_at"
    rating_col = ",ur.rating AS user_rating" if table == "user_ratings" else ""
    group_rating = ",ur.rating" if table == "user_ratings" else ""
    sql = f"""
        SELECT m.movie_id,m.title,m.tmdb_id,m.imdb_id{rating_col},COALESCE(AVG(dr.rating),0) AS average_rating,
               COUNT(DISTINCT dr.user_id) AS rating_count,COALESCE(array_agg(DISTINCT g.genre)
               FILTER (WHERE g.genre IS NOT NULL),'{{}}') AS genres
        FROM {source} JOIN movies m USING(movie_id)
        LEFT JOIN dataset_ratings dr ON dr.movie_id=m.movie_id LEFT JOIN movie_genres g ON g.movie_id=m.movie_id
        WHERE {owner}=%s GROUP BY m.movie_id,m.title,m.tmdb_id,m.imdb_id{group_rating}
        ORDER BY max({sort_time}) DESC
    """
    from app.movies import movie_row
    return [movie_row(row) for row in fetch_all(sql, (user_id,))]


@app.get("/api/v1/me/ratings")
def get_ratings(user_id: UUID = Depends(current_user_id)):
    return _user_movie_rows("user_ratings", user_id, True)


@app.put("/api/v1/me/ratings")
def set_rating(body: RatingInput, user_id: UUID = Depends(current_user_id), _: None = Depends(validate_csrf)):
    with connection() as conn:
        exists = conn.execute("SELECT 1 FROM movies WHERE movie_id=%s", (body.movie_id,)).fetchone()
        if not exists:
            raise HTTPException(status_code=404, detail="Película inexistente")
        conn.execute("""INSERT INTO user_ratings(user_id,movie_id,rating) VALUES (%s,%s,%s)
                      ON CONFLICT(user_id,movie_id) DO UPDATE SET rating=EXCLUDED.rating,updated_at=now()""",
                     (user_id, body.movie_id, body.rating))
    return {"movie_id": body.movie_id, "rating": body.rating}


@app.delete("/api/v1/me/ratings/{movie_id}")
def delete_rating(movie_id: int, user_id: UUID = Depends(current_user_id), _: None = Depends(validate_csrf)):
    with connection() as conn:
        conn.execute("DELETE FROM user_ratings WHERE user_id=%s AND movie_id=%s", (user_id, movie_id))
    return {"movie_id": movie_id, "deleted": True}


@app.get("/api/v1/me/favorites")
def favorites(user_id: UUID = Depends(current_user_id)):
    return _user_movie_rows("favorites", user_id)


@app.post("/api/v1/me/favorites/{movie_id}")
def add_favorite(movie_id: int, user_id: UUID = Depends(current_user_id), _: None = Depends(validate_csrf)):
    with connection() as conn:
        conn.execute("INSERT INTO favorites(user_id,movie_id) SELECT %s,movie_id FROM movies WHERE movie_id=%s ON CONFLICT DO NOTHING", (user_id, movie_id))
    return {"movie_id": movie_id, "saved": True}


@app.delete("/api/v1/me/favorites/{movie_id}")
def delete_favorite(movie_id: int, user_id: UUID = Depends(current_user_id), _: None = Depends(validate_csrf)):
    with connection() as conn:
        conn.execute("DELETE FROM favorites WHERE user_id=%s AND movie_id=%s", (user_id, movie_id))
    return {"movie_id": movie_id, "saved": False}


@app.get("/api/v1/me/watchlist")
def watchlist(user_id: UUID = Depends(current_user_id)):
    return _user_movie_rows("watchlist", user_id)


@app.post("/api/v1/me/watchlist/{movie_id}")
def add_watchlist(movie_id: int, user_id: UUID = Depends(current_user_id), _: None = Depends(validate_csrf)):
    with connection() as conn:
        conn.execute("INSERT INTO watchlist(user_id,movie_id) SELECT %s,movie_id FROM movies WHERE movie_id=%s ON CONFLICT DO NOTHING", (user_id, movie_id))
    return {"movie_id": movie_id, "saved": True}


@app.delete("/api/v1/me/watchlist/{movie_id}")
def delete_watchlist(movie_id: int, user_id: UUID = Depends(current_user_id), _: None = Depends(validate_csrf)):
    with connection() as conn:
        conn.execute("DELETE FROM watchlist WHERE user_id=%s AND movie_id=%s", (user_id, movie_id))
    return {"movie_id": movie_id, "saved": False}


@app.get("/api/v1/me/tags")
def get_tags(user_id: UUID = Depends(current_user_id)):
    return fetch_all("SELECT movie_id,tag FROM user_tags WHERE user_id=%s ORDER BY created_at DESC", (user_id,))


@app.post("/api/v1/me/tags/{movie_id}")
def add_tag(movie_id: int, body: TagInput, user_id: UUID = Depends(current_user_id), _: None = Depends(validate_csrf)):
    with connection() as conn:
        conn.execute("INSERT INTO user_tags(user_id,movie_id,tag) SELECT %s,movie_id,%s FROM movies WHERE movie_id=%s ON CONFLICT DO NOTHING",
                     (user_id, body.tag, movie_id))
    return {"movie_id": movie_id, "tag": body.tag}
