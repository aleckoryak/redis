# Publishhouse Backend

Simple Spring Boot REST backend for articles and comments.

## Tech

- Spring Boot (Web, Validation, Data JPA)
- PostgreSQL
- SQL initialization via `schema.sql` and `data.sql`

## API

- `POST /articles`
- `PUT /articles/{id}`
- `DELETE /articles/{id}`
- `GET /articles` (includes `averageRating` and `commentsCount`)
- `GET /articles/{id}` (single article with `averageRating` and `commentsCount`)
- `POST /articles/{id}/comments`
- `GET /articles/{id}/comments`
- `GET /trending` (one top article by average score; ties may return any top article)

## Data model

- `Article`: `id`, `title`, `text`
- `Comment`: `text`, `score (1..100)`
- Relationship: one article to many comments

## Seed data

- `data.sql` inserts 200 stub articles with generated numeric IDs (`1` ... `200`).

## Run (from repository root)

```powershell
mvn -pl publishhouse spring-boot:run
```

## Test (from repository root)

```powershell
mvn -pl publishhouse test
```

## Curl smoke flow (from repository root)

```powershell
PowerShell -ExecutionPolicy Bypass -File "publishhouse/smoke-curl.ps1"
```
