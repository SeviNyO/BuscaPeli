# Backend BuscaPeli

API REST con Python 3.12 y FastAPI, PostgreSQL mediante Psycopg 3 y sesiones firmadas con protección CSRF. Los algoritmos de `app/algorithms` son módulos puros, independientes de FastAPI y PostgreSQL.

## Ejecución local

La forma recomendada es `docker compose up --build` desde la raíz. Para correr FastAPI fuera de Docker:

```powershell
py -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r backend\requirements.txt
$env:DATABASE_URL='postgresql://buscapeli:tu-clave@localhost:5432/buscapeli'
$env:MOVIELENS_DATA_DIR='../csv'
$env:SESSION_SECRET='cambia-esto-por-un-secreto-aleatorio'
cd backend
uvicorn app.main:app --reload --port 8080
```

En el primer arranque se crea el esquema desde `schema.sql` y se importan los CSV de MovieLens: 1.500 películas más populares y usuarios con al menos cinco valoraciones de ese grupo. La carga se omite si ya existen películas.

Documentación interactiva: <http://localhost:8080/docs>.
