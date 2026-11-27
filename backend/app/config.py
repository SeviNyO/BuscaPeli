import os
import hashlib
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class Settings:
    database_url: str = os.getenv("DATABASE_URL", "")
    database_host: str = os.getenv("DATABASE_HOST", "localhost")
    database_port: int = int(os.getenv("DATABASE_PORT", "5432"))
    database_name: str = os.getenv("DATABASE_NAME", "buscapeli")
    database_user: str = os.getenv("DATABASE_USER", "buscapeli")
    database_password: str = os.getenv("DATABASE_PASSWORD", "buscapeli")
    dataset_dir: Path = Path(os.getenv("MOVIELENS_DATA_DIR", "../csv"))
    top_movies_limit: int = int(os.getenv("TOP_MOVIES_LIMIT", "1500"))
    min_user_ratings: int = int(os.getenv("MIN_USER_RATINGS", "5"))
    min_recommendation_rating: float = float(os.getenv("MIN_RECOMMENDATION_RATING", "4.0"))
    min_common_movies: int = int(os.getenv("MIN_COMMON_MOVIES", "2"))
    target_communities: int = int(os.getenv("TARGET_COMMUNITIES", "20"))
    tmdb_api_key: str = os.getenv("TMDB_API_KEY", "")
    session_secret: str = os.getenv("SESSION_SECRET") or hashlib.sha256(
        f"buscapeli-session:{os.getenv('DATABASE_URL', '')}:{os.getenv('DATABASE_PASSWORD', os.getenv('POSTGRES_PASSWORD', 'buscapeli'))}".encode()
    ).hexdigest()
    cookie_secure: bool = os.getenv("COOKIE_SECURE", "false").lower() == "true"


settings = Settings()
