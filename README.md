# BuscaPeli

Aplicación de recomendaciones de películas: Vue 3, Spring Boot 3, PostgreSQL y dos grafos bipartitos. MovieLens se importa desde `csv/` a PostgreSQL al primer inicio; las cuentas, ratings propios, favoritos y listas se guardan en tablas separadas.

## Inicio local recomendado

Requisitos: Docker Desktop con Compose. Desde la raíz del repo:

```powershell
Copy-Item .env.example .env
# Opcional: añade TMDB_API_KEY al archivo .env para pósteres y metadatos.
docker compose up --build
```

Abrir <http://localhost:8088>. El primer arranque crea el esquema y carga MovieLens desde `csv/`; puede tardar un poco. PostgreSQL conserva sus datos en el volumen `postgres_data`.

Para detener: `docker compose down`. **No uses `docker compose down -v`** salvo que quieras borrar la base y volver a importarla.

## Estructura

- `frontend/`: pantallas Vue, navegación y cliente HTTP.
- `backend/`: API REST Spring Boot, autenticación por sesión, acceso JDBC, importador y algoritmos independientes de Spring.
- `backend/src/main/resources/db/migration/`: esquema PostgreSQL versionado por Flyway.
- `csv/`: MovieLens Small original (`movies`, `ratings`, `tags`, `links`); se filtra e importa una sola vez si la tabla `movies` está vacía.
- `docker-compose.yml`: frontend/Nginx, backend y PostgreSQL para desarrollo o una VM.

## API

- `GET /api/v1/movies?search=titanik&genre=Ciencia%20ficción`: búsqueda con sugerencias aproximadas, filtro, orden y páginas.
- `GET /api/v1/movies/popular` y `/api/v1/movies/top-rated`.
- `GET /api/v1/movies/{id}`, `/{id}/details` y `/{id}/poster`.
- `GET /api/v1/recommendations/movies/{id}`: BFS, comunidades globales Kruskal/UFDS, Bellman-Ford, Merge Sort y selección voraz top-N.
- `GET /api/v1/recommendations/movies/{id}/by-genre`: BFS sobre película–género, Merge Sort y top-N.
- `GET /api/v1/recommendations/me`: recomendaciones desde los ratings de la cuenta.
- `/api/v1/auth/*` y `/api/v1/me/*`: sesión, ratings, favoritos, “Quiero ver” y tags propios.
- `GET /api/v1/health`: estado y conteos básicos.

Documentación y esquema: `backend/README.md` y `backend/src/main/resources/db/migration/V1__schema.sql`.
