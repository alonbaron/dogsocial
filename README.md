## dog-social / PawPals

Full-stack social network for dog owners.

### Project structure
- **backend**: Java 21, Spring Boot, JWT auth, MySQL, JPA.
- **frontend**: Vite + React (JS), React Router, Axios, Tailwind CSS.
- **docker-compose.yml**: MySQL + backend (built into a container).

### Prerequisites
- Docker Desktop (for MySQL + backend with `docker-compose up`).
- Node.js 18+ (you have Node 24) + npm (for the frontend).

### Backend (Docker)
From the project root:

```bash
docker-compose up --build
```

This will:
- Start **MySQL** (on host port `3307`, database `dog_social`).
- Apply the schema from `db/init/01_schema.sql`.
- Build the Spring Boot backend image and start it on `http://localhost:8080`.

The backend exposes REST API under `/api`, e.g.:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET /api/auth/me`
- `GET /api/feed`
- `POST /api/posts`
- `POST /api/playdates`

### Frontend (Vite + React)

1. Copy the example env and adjust if needed:

```bash
cd frontend
cp .env.example .env
```

2. Install dependencies (already done once, but repeat on new machines):

```bash
npm install
```

3. Run the dev server:

```bash
npm run dev
```

The app will be available at `http://localhost:5173`.

### Using the app
1. Visit `http://localhost:5173`.
2. **Register** a new user.
3. You’ll be redirected to the **Feed**:
   - Add, edit, and delete dog profiles from your profile page.
   - Create posts (optionally associated with a dog).
   - React (like/dislike) on posts and comments.
4. Use the **Playdates** page to:
   - Send playdate requests (future datetime, other user ID).
   - See incoming, upcoming, and past playdates.

### JWT & security
- Passwords are hashed with BCrypt.
- JWT is returned from `/api/auth/login` and `/api/auth/register`.
- The frontend stores JWT in `sessionStorage` and sends it as `Authorization: Bearer <token>`.
- Public endpoints include register, login, password reset, health checks, and media reads. All other endpoints require a valid JWT.

### Is the database up? (Troubleshooting)

From the project root:

1. **Containers running?**
   ```bash
   docker-compose ps
   ```
   You should see `dog-social-mysql` and `dog-social-backend` as **Up**. If not: `docker-compose up -d`.

2. **MySQL is on port 3307** on your machine (mapped from 3306 inside Docker). To check the port:
   - PowerShell: `Test-NetConnection -ComputerName localhost -Port 3307`
   - With MySQL client: `mysql -h 127.0.0.1 -P 3307 -u dogsocial -pdogsocial -e "SELECT 1"`

3. **Backend and DB logs** (when you get a 500, check here for the real error):
   ```bash
   docker-compose logs backend
   docker-compose logs mysql
   ```

4. **If you run the backend locally** (e.g. from IDE or `mvn spring-boot:run`), point it at port **3307**:
   - Set env: `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/dog_social?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - Otherwise the default `localhost:3306` won’t reach the Docker MySQL.

### Step-by-step: “Why can’t the app pull data?”

Work through these in order. Stop at the first step that fails and fix it.

**Step 1 — Are the containers running?**
```bash
docker-compose ps
```
- Both `dog-social-mysql` and `dog-social-backend` should be **Up**. If not: `docker-compose up -d` (or `docker-compose up --build` to see logs).

**Step 2 — Can the backend reach the database?**  
No login needed. In a browser or with curl:
```bash
curl http://localhost:8080/api/health/db
```
- **Success:** You get JSON like `{"status":"up","database":"ok","userCount":0}`. Database is fine; if the app still fails, the problem is auth (401) or a specific endpoint (500). Go to Step 4.
- **Connection refused / no response:** Backend isn’t running or isn’t on 8080. Start it (e.g. `docker-compose up`) or fix the port.
- **503 with `"database":"error"`:** The backend is up but can’t talk to MySQL. The JSON `message` will say why (e.g. “Communications link failure” = wrong host/port or MySQL not reachable). Then:
  - If you run the **backend in Docker**: MySQL might not be healthy yet; wait a minute and try again, or run `docker-compose logs mysql`.
  - If you run the **backend locally** (IDE or Maven): set `SPRING_DATASOURCE_URL=...localhost:3307/...` (see above). The default is `localhost:3306`, which doesn’t match Docker’s port 3307.

**Step 3 — Is MySQL reachable on the host?** (optional, if Step 2 fails)  
From your machine (not inside a container), MySQL is on port **3307**:
```bash
# PowerShell
Test-NetConnection -ComputerName localhost -Port 3307
```
- If this fails, start or fix the MySQL container: `docker-compose up -d mysql` and check `docker-compose logs mysql`.

**Step 4 — What error do you get from the app?**
- **401 Unauthorized:** You’re not logged in or the token is missing/expired. Log in again (or register); the app will store a new JWT.
- **500 Internal Server Error:** Backend threw an exception. Check the **response body** for the `detail` field (exception type and message). Also run `docker-compose logs backend` (or your IDE console) and look at the stack trace to see which line failed.
- **CORS / network errors in the browser:** Ensure the frontend is calling the same backend URL (e.g. `http://localhost:8080/api`) and that `APP_CORS_ALLOWED_ORIGINS` includes your frontend origin (e.g. `http://localhost:5173`).
