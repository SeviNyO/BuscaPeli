# Backend BuscaPeli

Spring Boot 3 / Java 17, PostgreSQL y Flyway. Las clases de `recommendation/algorithms` no dependen de Spring y tienen pruebas unitarias.

## Arranque

La opción sencilla es seguir el README de la raíz y usar Docker Compose. Para ejecutar Java directamente, se requiere JDK 17, Maven y PostgreSQL; desde `backend/`:

```powershell
$env:DATABASE_URL='jdbc:postgresql://localhost:5432/buscapeli'
$env:DATABASE_USER='buscapeli'
$env:DATABASE_PASSWORD='tu-clave-local'
$env:MOVIELENS_DATA_DIR='../csv'
mvn spring-boot:run
```

Flyway crea el esquema y el importador aplica el filtro: 1,500 películas más populares y usuarios con al menos 5 ratings en ese grupo. Si la tabla `movies` ya tiene datos, no se repite la importación.

## Pruebas

```powershell
mvn test
```

Las comunidades usuario–usuario se calculan globalmente al arrancar y se mantienen en memoria. Los valores `MIN_COMMON_MOVIES` y `TARGET_COMMUNITIES` se pueden ajustar desde el entorno.
