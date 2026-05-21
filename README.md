# PawPals

> A full-stack social network for dog owners, built with React, Spring Boot, MySQL, and JWT authentication.

PawPals is the public-facing name of this project. The repository slug is still `dogsocial`, but the app itself is PawPals: a place for dog owners to create profiles for their dogs, share posts, follow other users, and schedule playdates.

This is an educational portfolio project, licensed under the MIT License. See [LICENSE](LICENSE).

---

## What It Does

PawPals supports the core flows of a small social app:

- Account registration, login, JWT session handling, and password reset.
- User profiles with username, bio, avatar upload, followers, and following.
- Dog profiles with name, breed, bio, photo upload, edit, and delete.
- Feed posts with optional dog association, image upload, comments, and reactions.
- Browse and friends views for discovering users and posts.
- Playdate requests by username, with incoming, upcoming, and past views.

The app is intentionally modest in scope, but it has real full-stack edges: authentication, file upload, relational data modeling, pagination, ownership checks, and production-oriented error handling.

---

## Why This Repo Matters

This started as a working dog-owner social app and then went through a portfolio polish pass. The current commit history shows more than "it runs": it shows finding concrete backend risks and tightening them in small, reviewable commits.

Examples from the polish arc:

- Replaced an N+1 follow lookup in the followers modal with a batch query.
- Added a Maven wrapper so the backend can build without a local Maven install.
- Fixed a password-reset rate-limit memory leak caused by stale per-IP buckets.
- Converted duplicate unique-constraint races into clean `400` API errors.
- Added focused backend tests around the auth and exception-handling fixes.
- Removed stale planning notes and local-only files before making the repo public.

That is the same story I want this repo to tell publicly: a real student project, revisited with engineering judgment instead of rewritten into something fake.

---

## Tech Stack

### Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA / Hibernate
- MySQL 8
- JWT authentication
- BCrypt password hashing
- Maven wrapper

### Frontend

- Vite
- React
- React Router
- Axios
- Tailwind CSS
- `sessionStorage` token handling

### Infrastructure

- Docker Compose for MySQL and backend
- Vercel-oriented frontend config
- Environment-variable based backend configuration

---

## Architecture Notes

### Authentication and authorization

The backend is stateless. Login and registration return a JWT, and the frontend sends it as:

```text
Authorization: Bearer <token>
```

Public endpoints include registration, login, password reset, health checks, and media reads. All other API routes require a valid JWT. Service-layer checks enforce ownership for posts, comments, dog profiles, and profile updates.

### Data model

The main entities are:

| Entity | Role |
|---|---|
| `User` | Account identity, profile data, avatar, role |
| `Dog` | Dog profile owned by a user |
| `Post` | Feed content, optionally linked to a dog |
| `Comment` | Comment on a post |
| `PostReaction` | Like/dislike reaction per user and post |
| `CommentReaction` | Like/dislike reaction per user and comment |
| `Follow` | Follower/followed relationship |
| `Playdate` | Request between two users with status and scheduled time |
| `StoredImage` | Uploaded image bytes and metadata |
| `PasswordResetToken` | Hashed reset token with expiration and usage state |

### API shape

The backend exposes REST endpoints under `/api`, including:

| Area | Example endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/forgot-password`, `POST /api/auth/reset-password` |
| Users | `GET /api/users`, `GET /api/users/{userId}`, `PUT /api/users/me/profile` |
| Dogs | `POST /api/dogs`, `GET /api/dogs/{dogId}`, `PUT /api/dogs/{dogId}`, `DELETE /api/dogs/{dogId}` |
| Posts | `GET /api/feed`, `POST /api/posts`, `GET /api/posts/{postId}`, `PUT /api/posts/{postId}`, `DELETE /api/posts/{postId}` |
| Comments | `GET /api/posts/{postId}/comments`, `POST /api/posts/{postId}/comments`, `PUT /api/comments/{commentId}` |
| Reactions | `PUT /api/posts/{postId}/reaction`, `PUT /api/comments/{commentId}/reaction` |
| Follows | `POST /api/follows/{userId}`, `DELETE /api/follows/{userId}` |
| Playdates | `POST /api/playdates`, `GET /api/playdates/incoming`, `GET /api/playdates/upcoming`, `GET /api/playdates/past` |

Pagination uses standard Spring Data query params: `page` and `size`.

---

## How to Run

### Prerequisites

- Docker Desktop
- Node.js 18+
- JDK 21+ if running the backend outside Docker

### Backend and database with Docker

From the project root:

```bash
docker-compose up --build
```

This starts:

- MySQL on host port `3307`
- Database `dog_social`
- Spring Boot backend on `http://localhost:8080`

### Frontend

In a second terminal:

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

The app runs at:

```text
http://localhost:5173
```

The frontend calls the backend through:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## Local Backend Without Docker

If MySQL is running through Docker but the backend is started from an IDE or Maven, point Spring at the host-mapped database port:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3307/dog_social?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
```

Then run:

```powershell
cd backend
$env:JAVA_HOME="C:\Users\alonb\.jdks\corretto-23.0.2"
.\mvnw.cmd spring-boot:run
```

For tests:

```powershell
cd backend
$env:JAVA_HOME="C:\Users\alonb\.jdks\corretto-23.0.2"
.\mvnw.cmd test
```

---

## Troubleshooting

### Check the containers

```bash
docker-compose ps
```

Both `dog-social-mysql` and `dog-social-backend` should be up.

### Check backend database health

```bash
curl http://localhost:8080/api/health/db
```

Expected success response:

```json
{"status":"up","database":"ok","userCount":0}
```

### Check MySQL host port

```powershell
Test-NetConnection -ComputerName localhost -Port 3307
```

### Common errors

- `401 Unauthorized`: log in again; the JWT is missing or expired.
- `500 Internal Server Error`: check the response `detail` field and backend logs.
- CORS or network error: confirm `VITE_API_BASE_URL` points to `http://localhost:8080/api` and `APP_CORS_ALLOWED_ORIGINS` includes the frontend origin.

---

## Concepts Touched

- Stateless JWT auth with Spring Security
- Password hashing with BCrypt
- Password reset token hashing and expiration
- Rate limiting and in-memory cleanup
- REST API design with DTOs
- JPA relationships and unique constraints
- Handling duplicate-write races cleanly
- Pagination and feed composition
- Image upload and media endpoints
- React routing, protected routes, and API client interceptors
- Dockerized local development
- Conventional Commits as a portfolio narrative

---

## License

MIT License. See [LICENSE](LICENSE).
