from contextlib import contextmanager
from pathlib import Path

import psycopg
from psycopg.rows import dict_row

from app.config import settings


@contextmanager
def connection():
    if settings.database_url:
        conn = psycopg.connect(settings.database_url, row_factory=dict_row)
    else:
        conn = psycopg.connect(host=settings.database_host, port=settings.database_port, dbname=settings.database_name,
                               user=settings.database_user, password=settings.database_password, row_factory=dict_row)
    with conn:
        yield conn


def initialize_schema() -> None:
    schema_path = Path(__file__).resolve().parent.parent / "schema.sql"
    sql = schema_path.read_text(encoding="utf-8")
    with connection() as conn:
        conn.execute(sql)


def fetch_all(sql: str, params: tuple = ()) -> list[dict]:
    with connection() as conn:
        return list(conn.execute(sql, params).fetchall())


def fetch_one(sql: str, params: tuple = ()) -> dict | None:
    with connection() as conn:
        return conn.execute(sql, params).fetchone()
