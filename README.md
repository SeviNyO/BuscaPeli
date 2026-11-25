# BuscaPeli

## Descripción

BuscaPeli es una aplicación web de recomendación de películas construida sobre MovieLens Small. Relaciona usuarios y películas mediante un grafo bipartito ponderado; un segundo grafo relaciona películas y géneros. La interfaz permite buscar títulos, consultar información de TMDB, calificar películas, guardar favoritos y consultar recomendaciones explicables.

## Arquitectura

| Capa | Tecnología | Responsabilidad |
| --- | --- | --- |
| Interfaz | Vue 3, Vite | Pantallas, navegación, cliente REST y estado de sesión. |
| Servidor web | Nginx | Entrega el frontend y redirige `/api/` al backend. En la VM, otro Nginx recibe el tráfico público por HTTPS y redirige las solicitudes HTTP. |
| API | Spring Boot 3, Java 17 | Sesiones, acceso a datos, importación y recomendaciones. |
| Persistencia | PostgreSQL 16, Flyway | Catálogo MovieLens, cuentas y datos propios de cada usuario. |
| Metadatos | API de TMDB | Pósteres, fondos, sinopsis y reparto cuando existe `tmdb_id` y se configura la clave. |

El despliegue utiliza Docker Compose con tres servicios: `frontend`, `backend` y `db`. Solo el servidor web expone un puerto; el backend y PostgreSQL permanecen en la red interna de Compose. En la VM el puerto de Compose se vincula a `127.0.0.1:8088` y Nginx publica la aplicación en los puertos 80 y 443.

## Organización del repositorio

```text
backend/                     API Spring Boot, algoritmos, pruebas y migraciones Flyway
csv/                         Archivos originales movies, ratings, tags y links
deploy/                      Configuración Nginx, instalación y unidades systemd de la VM
frontend/                    Aplicación Vue, cliente HTTP y componentes
docker-compose.yml           Servicios PostgreSQL, backend y frontend
.env.example                 Variables de entorno sin credenciales
```

Al primer inicio, Flyway crea el esquema y el importador selecciona las 1 500 películas más calificadas, junto a los usuarios con al menos cinco valoraciones en ese conjunto. Se obtienen 610 usuarios y 71 523 calificaciones. La importación no se repite cuando la tabla de películas ya contiene datos. PostgreSQL mantiene sus datos en el volumen `postgres_data`.

## Recomendaciones

- **Para ti — `GET /api/v1/recommendations/me`:** parte de hasta cinco películas que la cuenta calificó con al menos 4 estrellas. Combina candidatas de la recomendación colaborativa y elimina películas que sirvieron como punto de partida. Sin valoraciones suficientes presenta películas populares.
- **Película elegida — `GET /api/v1/recommendations/movies/{id}`:** BFS encuentra películas bien calificadas por usuarios que también valoraron positivamente la película inicial; Kruskal y UFDS forman comunidades globales, Bellman-Ford calcula el costo de afinidad y Merge Sort ordena los resultados.
- **Por género — `GET /api/v1/recommendations/movies/{id}/by-genre`:** BFS busca películas que comparten géneros con la seleccionada; Merge Sort prioriza la cantidad de géneros compartidos y la calificación promedio.

La interfaz también consulta `/api/v1/movies`, `/api/v1/movies/popular`, `/api/v1/movies/top-rated`, `/api/v1/movies/{id}/details` y `/api/v1/movies/{id}/poster`. Las cuentas y colecciones usan `/api/v1/auth/*` y `/api/v1/me/*`. El estado del catálogo puede consultarse en `GET /api/v1/health`.

## Ejecución local

Se requiere Docker Engine con el complemento Compose. Copiar `.env.example` a `.env`, definir `POSTGRES_PASSWORD` y, si se requieren imágenes y metadatos, `TMDB_API_KEY`. `.env` no forma parte del repositorio.

```bash
cp .env.example .env
docker compose up -d --build
docker compose ps
```

La interfaz queda en <http://localhost:8088> y el estado de la API en <http://localhost:8088/api/v1/health>. El primer arranque incluye la importación del dataset. Para detener los servicios se utiliza `docker compose down`; la opción `-v` elimina el volumen de PostgreSQL y exige una nueva importación.

Las verificaciones independientes del despliegue se ejecutan con `mvn test` desde `backend/` y con `npm ci`, `npm run test:unit -- --run` y `npm run build` desde `frontend/`.

## Despliegue y operación en la VM

La instalación de referencia reside en `/home/ubuntu/BuscaPeli` y se accede por <https://buscapeli.duckdns.org/>. Las solicitudes a `http://buscapeli.duckdns.org/` y a la IP pública se redirigen al dominio HTTPS. El archivo `.env` de la VM debe incluir una contraseña propia, `HTTP_BIND=127.0.0.1`, `HTTP_PORT=8088`, `JAVA_TOOL_OPTIONS=-Xmx384m` y `COOKIE_SECURE=true`. Se reserva 1 GiB de intercambio para compilar en instancias de memoria reducida.

`deploy/setup-vm.sh` configura Docker, Nginx y el servicio `buscapeli-deploy.service`. El timer `buscapeli-deploy.timer` comprueba `origin/main` cada cinco minutos; `deploy/vm-redeploy.sh` actualiza el checkout con avance rápido, reconstruye los contenedores cuando cambia el commit y registra la versión desplegada después de comprobar la salud de la API. La base permanece en su volumen entre despliegues.

```bash
sudo systemctl status buscapeli-deploy.timer
sudo systemctl start buscapeli-deploy.service
journalctl -u buscapeli-deploy.service -n 80 --no-pager
docker compose ps
```

`deploy/enable-https.sh` emite un certificado Let's Encrypt mediante el desafío HTTP-01, instala la configuración TLS de Nginx y activa la renovación periódica de `certbot.timer`. El hook `deploy/reload-nginx.sh` recarga Nginx tras cada renovación. `deploy/nginx-bootstrap.conf` sirve temporalmente el desafío HTTP antes de la primera emisión. La clave privada y el certificado residen únicamente en `/etc/letsencrypt` de la VM.

## Datos externos

MovieLens Small pertenece a GroupLens Research. La API de TMDB aporta metadatos visuales, pero no interviene en los algoritmos de recomendación. BuscaPeli no está avalada por TMDB.
