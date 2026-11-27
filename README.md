# BuscaPeli

BuscaPeli recomienda películas de MovieLens Small con dos grafos bipartitos: usuario–película y película–género. La interfaz permite buscar títulos, consultar metadatos de TMDB, calificar películas, gestionar listas y obtener recomendaciones explicables.

## Arquitectura

| Capa | Tecnología | Responsabilidad |
| --- | --- | --- |
| Interfaz | Vue 3, Vite | Pantallas, navegación y cliente REST. |
| Servidor web | Nginx | Entrega Vue y proxy de `/api/` hacia la API. |
| API | Python 3.12, FastAPI | Sesión/CSRF, importación, acceso a datos y recomendaciones. |
| Persistencia | PostgreSQL 16, Psycopg 3 | MovieLens, cuentas y datos propios de cada cuenta. |
| Metadatos | TMDB API opcional | Póster, sinopsis, fecha y reparto. |

Docker Compose ejecuta `frontend`, `backend` y `db`; PostgreSQL y la API no publican puertos al exterior. En la VM, Nginx sirve HTTPS y proxy a `127.0.0.1:8088`.

## Datos MovieLens

Al primer inicio se importan los CSV originales de `csv/`: las 1.500 películas más populares, ratings de usuarios con al menos cinco calificaciones de ese grupo y sus tags. `links.csv` aporta `tmdbId`. La importación se omite cuando `movies` ya contiene datos. Ratings y etiquetas MovieLens permanecen separados de las cuentas y sus listas.

## Recomendaciones

- **Para ti** (`GET /api/v1/recommendations/me`): usa hasta cinco películas que la cuenta calificó con 4 o más; combina recomendaciones colaborativas y excluye las películas semilla. Si no hay ratings suficientes, ofrece populares.
- **Colaborativa** (`GET /api/v1/recommendations/movies/{id}`): BFS recorre el grafo usuario–película; Kruskal y UFDS precalculan comunidades globales; Bellman-Ford calcula afinidad `5.5 - rating`; Merge Sort ordena por costo y el voraz retorna top-N.
- **Por género** (`GET /api/v1/recommendations/movies/{id}/by-genre`): BFS explora película–género, Merge Sort prioriza géneros compartidos y rating promedio, y el voraz selecciona top-N.

Los algoritmos están en `backend/app/algorithms/`, separados de FastAPI y PostgreSQL. Se adaptaron a la estructura de listas de adyacencia de los notebooks del curso. Tags se persisten, pero no participan del recomendador.

## Ejecución local

Requisitos: Docker Engine y Docker Compose. Copia `.env.example` a `.env`, cambia `POSTGRES_PASSWORD` y `SESSION_SECRET`; `TMDB_API_KEY` es opcional.

```bash
cp .env.example .env
docker compose up -d --build
docker compose ps
```

Interfaz: <http://localhost:8088>. Salud de API: <http://localhost:8088/api/v1/health>. Para apagar, `docker compose down`; evita `-v` si quieres conservar PostgreSQL. FastAPI/Swagger: <http://localhost:8088/docs>.

Pruebas de algoritmos: desde la raíz, `PYTHONPATH=backend python -m unittest discover -s backend/tests -v`.

## Despliegue en la VM

El repositorio está configurado para `https://buscapeli.duckdns.org/`. La VM `/home/ubuntu/BuscaPeli` comprueba `origin/main` cada cinco minutos con `buscapeli-deploy.timer`; tras un push, `deploy/vm-redeploy.sh` hace fast-forward, reconstruye Compose y verifica `/api/v1/health` antes de marcar la revisión desplegada. `deploy/setup-vm.sh` configura Docker y Nginx; `deploy/enable-https.sh` gestiona TLS con Let's Encrypt.

El `.env` privado de la VM debe conservar `POSTGRES_PASSWORD`, definir un `SESSION_SECRET` propio y usar `HTTP_BIND=127.0.0.1`, `HTTP_PORT=8088` y `COOKIE_SECURE=true`. Si `SESSION_SECRET` no está definido, el backend deriva una clave estable de las credenciales privadas de PostgreSQL. No publiques el `.env` ni secretos en Git.

Operación:

```bash
sudo systemctl status buscapeli-deploy.timer
sudo systemctl start buscapeli-deploy.service
journalctl -u buscapeli-deploy.service -n 80 --no-pager
docker compose ps
```

MovieLens pertenece a GroupLens. Los metadatos TMDB no intervienen en los algoritmos; BuscaPeli no está avalada por TMDB.
