# Fix Avatar Upload / Display Bug

The avatar upload UI exists but doesn't work. After tracing the full pipeline (frontend component → upload handler → backend `AvatarService` → security config), I found **two bugs in the frontend** and one **minor security gap** in the backend.

## Root Causes

### Bug 1: `Avatar.jsx` — stale `failed` state never resets
When `Avatar` first renders and the user has no avatar, the `<img>` fires `onError`, setting `failed = true`. This state **never resets** — even when the `version` or `userId` prop changes (e.g. after uploading a new avatar). So the component stays stuck showing the initials fallback forever.

### Bug 2: `UserProfilePage.jsx` line 56 — broken arrow function
```js
setProfile((prev) => prev && { ...prev })
//                          ^ JS sees this as a BLOCK, not an object literal
```
Because `{` immediately follows `=>`, JavaScript interprets `{ ...prev }` as a **block statement**, not an object-return. This silently returns `undefined`, wiping the profile state after upload "succeeds".

### Security gap: `SecurityConfig.java` — avatar upload is publicly accessible  
The `AntPathRequestMatcher("/api/users/*/avatar")` rule uses `permitAll()` but matches **all HTTP methods** (GET *and* POST). This means `POST /api/users/me/avatar` is permitted without auth. While `AvatarService` internally calls `SecurityUtils.requireUserId()` which would throw, the request still bypasses Spring Security's auth check — a defense-in-depth concern.

## Proposed Changes

### Frontend — Avatar Component

#### [MODIFY] [Avatar.jsx](file:///c:/Users/alonb/Desktop/dog-social/frontend/src/components/Avatar.jsx)
- Add a `useEffect` that resets `failed` back to `false` whenever `userId` or `version` changes
- This lets the `<img>` retry loading after a new avatar upload or navigation to a different profile

```diff
+  useEffect(() => {
+    setFailed(false)
+  }, [userId, version])
```

---

### Frontend — User Profile Page

#### [MODIFY] [UserProfilePage.jsx](file:///c:/Users/alonb/Desktop/dog-social/frontend/src/pages/UserProfilePage.jsx)
- Fix arrow function to wrap the object literal in parentheses

```diff
-  setProfile((prev) => prev && { ...prev })
+  setProfile((prev) => prev && ({ ...prev }))
```

---

### Backend — Security Config

#### [MODIFY] [SecurityConfig.java](file:///c:/Users/alonb/Desktop/dog-social/backend/src/main/java/com/dogsocial/security/SecurityConfig.java)
- Restrict the `permitAll()` avatar rule to `GET` method only, so only reading avatars is public

```diff
-  .requestMatchers(new AntPathRequestMatcher("/api/users/*/avatar")).permitAll()
+  .requestMatchers(new AntPathRequestMatcher("/api/users/*/avatar", "GET")).permitAll()
```

## Verification Plan

### Browser Test
1. Start the backend with `docker-compose up`
2. Start the frontend with `cd frontend && npm run dev`
3. Register a new user or log in
4. Navigate to your profile page (`/users/{yourId}`)
5. Click the pink "+" button on the avatar circle
6. Select a JPEG/PNG image (under 2MB)
7. **Expected**: the avatar image appears in place of the initials — both in the profile header and in the navbar
8. Refresh the page — avatar should still be visible
9. Upload a different image — the avatar should update without needing a page refresh
