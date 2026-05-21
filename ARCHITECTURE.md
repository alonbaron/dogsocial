# dog-social architecture

## Goals
- **Simple monorepo**: `backend/` (Spring Boot) + `frontend/` (Vite/React) + root `docker-compose.yml`.
- **Stateless security**: JWT `Authorization: Bearer <token>` for protected endpoints.
- **DTO-first API**: controllers never expose entities directly.
- **Efficient feeds**: pagination everywhere; reaction counts + “my reaction” computed with minimal queries.

## Tech stack
### Backend
- Java 21, Spring Boot, Spring Security
- MySQL + JPA (Hibernate)
- JWT authentication (stateless)
- BCrypt password hashing
- Maven, REST API, Bean Validation

### Frontend
- Vite + React (JSX)
- React Router
- Axios
- Tailwind CSS
- JWT stored in **sessionStorage** (and mirrored in memory) + attached via Authorization header

## Domain model
### Entities
- **User**
  - `id`, `email` (unique), `passwordHash`, `role` (USER), `createdAt`
- **Dog**
  - `id`, `ownerId` (User), `name`, optional `breed`, optional `bio`, `createdAt`
- **Post**
  - `id`, `authorId` (User), optional `dogId` (Dog), `caption` (<=300), `createdAt`, `updatedAt`
- **Comment**
  - `id`, `postId` (Post), `authorId` (User), `content` (<=300), `createdAt`, `updatedAt`
- **PostReaction**
  - `id`, `postId`, `userId`, `type` (LIKE/DISLIKE), `createdAt`
  - unique: (`postId`, `userId`)
- **CommentReaction**
  - `id`, `commentId`, `userId`, `type` (LIKE/DISLIKE), `createdAt`
  - unique: (`commentId`, `userId`)
- **Follow**
  - `id`, `followerId`, `followedId`, `createdAt`
  - unique: (`followerId`, `followedId`)
- **Playdate**
  - `id`, `requesterId`, `recipientId`, `scheduledAt` (future), `status` (PENDING/APPROVED/DECLINED/CANCELED), `note`, `createdAt`, `updatedAt`

### Key constraints
- Cannot follow yourself.
- Cannot create playdate with yourself.
- `caption` and comment `content` max 300 chars.
- Reactions are exclusive: LIKE / DISLIKE / NONE (NONE deletes reaction row).

## API conventions
- All endpoints are prefixed with `/api`.
- Public endpoints include register, login, password reset, health checks, and media reads. All other endpoints require JWT.
- **Pagination**: standard Spring Data query params: `page` (0-based), `size`.
- **Page response DTO**:
  - `items`, `page`, `size`, `totalItems`, `totalPages`, `hasNext`
- **Errors**: consistent JSON envelope with `timestamp`, `status`, `error`, `message`, `path`, and optional `fieldErrors`.

## Authentication & Authorization
- Register: creates USER with BCrypt-hashed password.
- Login: returns JWT + basic user data.
- `/api/auth/me`: returns the authenticated user DTO.
- Authorization rules:
  - Post edit/delete: author only
  - Comment edit/delete: author only
  - Dog edit/delete: owner only

## Feed semantics
Feed returns posts authored by:
- the current user
- users the current user follows

Each post includes:
- `likesCount`, `dislikesCount`
- `myReaction`: LIKE/DISLIKE/NONE

Each comment includes:
- `likesCount`, `dislikesCount`
- `myReaction`: LIKE/DISLIKE/NONE

## Proposed endpoints (high-level)
### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

### Users / Profiles
- `GET /api/users/{userId}` (public-ish but still JWT-protected in this scaffold)
- `GET /api/users/{userId}/dogs`

### Dogs
- `POST /api/dogs`
- `GET /api/dogs/{dogId}`
- `PUT /api/dogs/{dogId}` (owner only)
- `DELETE /api/dogs/{dogId}` (owner only)

### Posts / Feed
- `GET /api/feed?page&size`
- `POST /api/posts`
- `GET /api/posts/{postId}`
- `PUT /api/posts/{postId}` (owner only)
- `DELETE /api/posts/{postId}` (owner only)
- `GET /api/dogs/{dogId}/posts?page&size`
- `PUT /api/posts/{postId}/reaction` body: `{ "type": "LIKE|DISLIKE|NONE" }`

### Comments
- `GET /api/posts/{postId}/comments?page&size`
- `POST /api/posts/{postId}/comments`
- `PUT /api/comments/{commentId}` (author only)
- `DELETE /api/comments/{commentId}` (author only)
- `PUT /api/comments/{commentId}/reaction` body: `{ "type": "LIKE|DISLIKE|NONE" }`

### Follow
- `POST /api/follows/{userId}` (follow)
- `DELETE /api/follows/{userId}` (unfollow)
- `GET /api/users/{userId}/followers`
- `GET /api/users/{userId}/following`

### Playdates
- `POST /api/playdates` (create request)
- `PUT /api/playdates/{playdateId}/status` body: `{ "status": "APPROVED|DECLINED|CANCELED" }`
- `GET /api/playdates/incoming` (PENDING to me)
- `GET /api/playdates/upcoming` (APPROVED in the future involving me)
- `GET /api/playdates/past` (APPROVED in the past involving me)

## Data access approach
- Use repositories for persistence.
- Services encapsulate authorization rules and business logic.
- For counts + myReaction:
  - count via aggregated queries (`COUNT` with `type`)
  - my reaction via `findBy...AndUserId`
  - results composed in service-layer DTO mapping.
