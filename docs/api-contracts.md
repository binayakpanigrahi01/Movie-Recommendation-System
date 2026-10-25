# Movie Recommender — API Contracts

Last updated: 27 Sep 2026 · Related docs: [PRD](./prd.md) · [TRD](./trd.md) · [Forms Spec](./forms-spec.md) · [Database Schema](./database-schema.md)

Exact request and response JSON for every v1 endpoint listed in the [TRD](./trd.md#api-endpoints-v1). This is the contract both the frontend and backend build against, so field names never have to be guessed or reconciled later.

## Conventions

- **All fields are camelCase.** Database columns are snake_case (see the Database Schema); the API layer converts between the two.
- **Auth:** every endpoint marked "Auth: yes" requires `Authorization: Bearer <token>`, obtained from `/auth/signup` or `/auth/login`.
- **Errors** always use the envelope defined in the TRD:
  ```json
  { "error": { "code": "SOME_CODE", "message": "Human-readable message" } }
  ```
- **`genreId` / `keywordId` values refer to this application's own `genres.id` / `keywords.id`** (as returned by `GET /genres`), never TMDB's raw numeric IDs directly. The mapping between the two is internal to the backend.
- **`mediaType` has two vocabularies in play:** session-facing fields use the product's own words — `"movie"`, `"series"`, `"either"` — matching S1. Title- and genre-facing fields use TMDB's own words — `"movie"`, `"tv"` — since that's what the cached data mirrors. The backend translates `"series"` → `"tv"` internally when querying `titles` or `genres`; the frontend never needs to make this translation itself.
- **The host's age band is always computed server-side** from their profile's `birthYear` — any `ageBand` submitted for `personIndex: 0` (the host) is ignored. Every other person's `ageBand` comes directly from their Q2 answer, since guests have no stored profile.
- Dates are ISO 8601 strings (e.g. `"2026-09-27T14:00:00Z"`).

## Auth

### `POST /auth/signup`

Auth: no

```json
// Request
{ "name": "Amey", "email": "amey@example.com", "password": "correcthorse123" }
```

```json
// 201 Response — signs the user in immediately, straight into Profile setup
{
  "token": "eyJhbGciOi...",
  "user": { "id": 1, "name": "Amey", "email": "amey@example.com" }
}
```

Errors: `409 EMAIL_ALREADY_REGISTERED` · `400 VALIDATION_ERROR` (e.g. password under 8 characters)

### `POST /auth/login`

Auth: no

```json
// Request
{ "email": "amey@example.com", "password": "correcthorse123" }
```

```json
// 200 Response
{
  "token": "eyJhbGciOi...",
  "user": { "id": 1, "name": "Amey", "email": "amey@example.com" }
}
```

Errors: `401 INVALID_CREDENTIALS`

### `POST /auth/forgot-password`

Auth: no

```json
// Request
{ "email": "amey@example.com" }
```

```json
// 200 Response — identical whether or not the email is registered, on purpose (see UI/UX Spec, screen 1)
{ "message": "If that email is registered, we've sent a reset link." }
```

Errors: none — this endpoint never reveals whether an email exists.

### `POST /auth/reset-password`

Auth: no

```json
// Request
{ "token": "a1b2c3...", "newPassword": "newpassword456" }
```

```json
// 200 Response
{ "message": "Password updated." }
```

Errors: `410 RESET_TOKEN_EXPIRED` · `400 RESET_TOKEN_INVALID` · `400 VALIDATION_ERROR`

## Profile

### `GET /profile`

Auth: yes

```json
// 200 Response
{
  "name": "Amey",
  "email": "amey@example.com",
  "birthYear": 1998,
  "subtitlesOk": true,
  "languages": ["hi", "en"],
  "favoriteGenres": [3, 7],
  "neverWantGenres": [12],
  "lovedTitleIds": [104, 512, 998],
  "eraPreference": "any_era"
}
```

`name` and `email` come from the account (`users`), not the profile table — there's only one place a display name is stored (see the Forms Spec's note on removing the duplicate P1 question).

Errors: `404 PROFILE_NOT_FOUND` (only possible in the brief window between sign-up and finishing the profile wizard)

### `PUT /profile`

Auth: yes. Creates the profile on first call, updates it on later calls.

```json
// Request — every field required except lovedTitleIds
{
  "birthYear": 1998,
  "subtitlesOk": true,
  "languages": ["hi", "en"],
  "favoriteGenres": [3, 7],
  "neverWantGenres": [12],
  "lovedTitleIds": [104, 512],
  "eraPreference": "any_era"
}
```

```json
// 200 Response — same shape as GET /profile
```

Errors: `400 VALIDATION_ERROR` (`favoriteGenres` over 5, `lovedTitleIds` over 5, `languages` empty, etc.)

## Genres

### `GET /genres`

Auth: yes. Optional query param `?mediaType=movie` or `?mediaType=tv` narrows the list; omitted returns the full deduplicated set (used by the profile wizard's P6/P7, which aren't tied to one media type yet).

```json
// 200 Response
{
  "genres": [
    { "id": 3, "name": "Comedy", "mediaType": "movie" },
    { "id": 3, "name": "Comedy", "mediaType": "tv" },
    { "id": 12, "name": "Horror", "mediaType": "movie" }
  ]
}
```

## Titles

### `POST /titles/search`

Auth: yes. Used by the P8 and Q8 pickers. Every result is immediately upserted into `titles` (see the TRD's caching note), so its `id` can be referenced right away.

```json
// Request
{ "query": "inception", "mediaType": "movie" }
```

```json
// 200 Response
{
  "results": [
    { "id": 104, "tmdbId": 27205, "mediaType": "movie", "title": "Inception", "posterPath": "/....jpg", "releaseDate": "2010-07-16" }
  ]
}
```

Errors: `502 TMDB_UNREACHABLE`

### `GET /titles/{id}`

Auth: yes. Powers the Title detail screen. `{id}` is this application's own `titles.id`.

```json
// 200 Response
{
  "id": 104,
  "tmdbId": 27205,
  "mediaType": "movie",
  "title": "Inception",
  "overview": "A thief who steals corporate secrets...",
  "posterPath": "/....jpg",
  "backdropPath": "/....jpg",
  "releaseDate": "2010-07-16",
  "runtimeMinutes": 148,
  "seriesStatus": null,
  "genreNames": ["Action", "Science Fiction"],
  "cast": [{ "name": "Actor A", "character": "Cobb", "photoUrl": "/....jpg" }],
  "certificationRaw": "UA 13+",
  "certificationBand": "UA13",
  "imdbRating": 8.8,
  "rottenTomatoesRating": 87,
  "metacriticRating": 74
}
```

Errors: `404 TITLE_NOT_FOUND`

## Sessions

### `POST /sessions/start`

Auth: yes. Submits S1–S4.

```json
// Request
{
  "mediaType": "movie",
  "groupSize": 4,
  "timeLimit": "90_120",
  "occasion": "friends_hangout"
}
```

```json
// 201 Response
{ "sessionId": 501 }
```

Errors: `400 VALIDATION_ERROR`

### `POST /sessions/{id}/submit`

Auth: yes. Submits every person's Q1–Q8 answers at once (collected across the handoff loop, held in frontend state until this point) and triggers the first round of matching. `personIndex: 0` is always the host.

```json
// Request
{
  "people": [
    {
      "personIndex": 0,
      "name": "Amey",
      "ageBand": "18_plus",
      "moods": ["Laugh"],
      "genresUpFor": [3, 5],
      "hardNoGenres": [12],
      "languagesOk": ["hi", "en"],
      "intensity": "balanced",
      "somethingLikeTitleId": null
    },
    {
      "personIndex": 1,
      "name": "Rahul",
      "ageBand": "16_17",
      "moods": ["Thrill", "Laugh"],
      "genresUpFor": [1, 3],
      "hardNoGenres": [],
      "languagesOk": ["hi"],
      "intensity": "light",
      "somethingLikeTitleId": 104
    }
  ]
}
```

```json
// 200 Response — rank 1-5, fully formed, no partial state
{
  "sessionId": 501,
  "people": [{ "personIndex": 0, "name": "Amey" }, { "personIndex": 1, "name": "Rahul" }],
  "results": [
    {
      "rank": 1,
      "titleId": 210,
      "title": "3 Idiots",
      "posterPath": "/....jpg",
      "mediaType": "movie",
      "year": 2009,
      "runtimeMinutes": 170,
      "certificationRaw": "U",
      "certificationBand": "U",
      "groupScore": 88.5,
      "badge": "none",
      "aiExplanation": "A feel-good comedy that fits Amey's love of laughs and Rahul's thrill-seeking side.",
      "personScores": [
        { "personIndex": 0, "score": 91 },
        { "personIndex": 1, "score": 86 }
      ]
    }
  ]
}
```

Errors: `404 SESSION_NOT_FOUND` · `502 TMDB_UNREACHABLE` · `502 GROQ_UNREACHABLE` (explanations degrade to a generic line rather than failing the request)

### `POST /sessions/{id}/more`

Auth: yes. No request body — the session already has every person's answers stored. Re-runs filtering and scoring, excluding titles already in this session's results, and computes rank 6–10.

```json
// 200 Response — same shape as /submit's response, rank 6-10
{
  "sessionId": 501,
  "people": [{ "personIndex": 0, "name": "Amey" }, { "personIndex": 1, "name": "Rahul" }],
  "results": [ { "rank": 6, "titleId": 340, "...": "same shape as above" } ]
}
```

Errors: `404 SESSION_NOT_FOUND` · `502 TMDB_UNREACHABLE` · `502 GROQ_UNREACHABLE`

### `GET /sessions/{id}/results`

Auth: yes. Re-fetches whatever has been computed so far (rank 1–5, or 1–10 if "Show more" was already clicked) — e.g. after a page refresh.

```json
// 200 Response — same shape as /submit's response
```

Errors: `404 SESSION_NOT_FOUND`
