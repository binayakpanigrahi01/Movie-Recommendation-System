# 🎬 Movie Recommender

A group movie-night picker. One host starts a session — what to watch, group size, time available, occasion — then each person in the room answers a short private form on the same shared device. The backend runs a group-fairness matching engine and returns five titles everyone can agree on, each with a plain-language, AI-generated reason.

Solo portfolio project. Built and demoed by one developer; not intended for public/multi-household deployment (see [PRD](docs/prd.md) for scope).

![System architecture](diagrams/architecture.visual-check.1440x900.light.png)

*More diagrams (request sequence, matching engine, data flow, session lifecycle) live in [`diagrams/`](diagrams/README.md).*

## The problem

Picking something to watch is easy alone but slow in a group — everyone has a different mood, and streaming apps only personalize for one account, not a room full of people. This app asks a short set of questions once per person, then returns a short list the whole group can agree on instead of twenty minutes of scrolling.

## How it works

1. **Account & profile** — sign up once, answer a one-time profile (languages, favorite genres, genres to avoid, a few loved titles).
2. **Start a session** — the host picks what to watch, group size, time available, and the occasion.
3. **Everyone's turn** — the device is passed around; each person answers a short, mostly-clickable form. Answers are hidden once submitted so nobody sees or copies anyone else's.
4. **Matching** — hard filters remove anything that's a no for any one person (too intense for the youngest viewer, a disliked genre, a language nobody shares, too long for the time available); what's left is scored so the *group* comes out happy, not just the majority.
5. **Results** — five picks, each with a plain-language reason and a per-person fit indicator. The group can ask for five more or open a title for full detail.

Full product scope and the v1–v6 roadmap are in the [PRD](docs/prd.md).

## Tech stack

| Layer | Choice |
| --- | --- |
| Frontend | React 19 + Vite + TypeScript (desktop web, no fixed pixel widths) |
| Backend | Spring Boot 3.3 (Java 21) REST API |
| Database | MySQL |
| Movie/TV metadata | TMDB API (primary), OMDb (optional secondary ratings) |
| AI explanations | Groq (LLM) |
| Auth | Email + password, stateless JWT |
| Email | Brevo SMTP (password reset) |

The frontend never calls TMDB, OMDb, or Groq directly — every external call is proxied through the backend so API keys stay server-side.

## Project status

This is an active build, tracked phase-by-phase in [`Plan/to-do.md`](Plan/to-do.md).

- ✅ **Done:** environment & scaffolding, database schema + JPA entities, auth (signup/login/password reset), user profile, TMDB integration + local title caching, matching engine (hard filters, per-person scoring, misery-threshold retry), all session endpoints (`/sessions/start`, `/submit`, `/more`, `/results`)
- 🚧 **In progress:** Groq LLM integration for AI explanations (backend currently returns a templated placeholder reason)
- ⏳ **Not started:** the entire frontend UI (design system, all 9 screens), end-to-end integration testing

Check [`Plan/to-do.md`](Plan/to-do.md) before assuming an endpoint or screen exists — don't rely on this README alone for current implementation state.

## Architecture

```
Browser (React) <--JSON over HTTPS, JWT Bearer--> Spring Boot API <--> MySQL
                                                          |
                                                          +--> TMDB API
                                                          +--> OMDb API (optional)
                                                          +--> Groq API
                                                          +--> Brevo SMTP (password reset)
```

- **Auth:** stateless JWT (24h access token), validated by a Spring Security filter on every route except `/auth/**`. The frontend keeps the token in memory, never `localStorage`.
- **Core request flow:** `POST /sessions/start` → per-person handoff loop (held in frontend state only) → `POST /sessions/{id}/submit` does the whole match in one round trip: SQL hard filters → per-person scoring (genre 35 / mood 25 / light-intense 15 / similarity 15 / era 10) → misery-threshold retry (40 → 30 → 20) → concurrent Groq calls for the top 5 → single response.
- **Caching:** TMDB/OMDb responses are cached in MySQL's `titles` table rather than fetched per-request. `/titles/search` upserts a minimal row immediately; full detail (cast, keywords, certification) is fetched lazily only when a title becomes a match candidate or its detail page opens.

Diagrams for all of the above (with an interactive HTML viewer for each) are in [`diagrams/`](diagrams/README.md). Full architecture, API contracts, and matching-engine design are in the [TRD](docs/trd.md).

## Documentation

| Doc | Covers |
| --- | --- |
| [PRD](docs/prd.md) | Product scope, problem, versions v1–v6 |
| [TRD](docs/trd.md) | Architecture, API endpoints, matching engine, error handling — **read this first for backend work** |
| [Database Schema](docs/database-schema.md) | Tables, columns, keys, relationships |
| [API Contracts](docs/api-contracts.md) | Request/response shapes per endpoint |
| [Forms Spec](docs/forms-spec.md) | Per-question logic, how answers combine for a group |
| [UI/UX Spec](docs/ui-ux-spec.md) | Screen-by-screen UX behavior |
| [Wireframes](docs/wireframes.md) | Screen layouts |
| [Plan/plan.md](Plan/plan.md) | Build architecture plan |
| [Plan/to-do.md](Plan/to-do.md) | Phase-by-phase checklist — what's actually implemented |
| [diagrams/](diagrams/README.md) | Architecture, sequence, workflow, data-flow, and lifecycle diagrams |

## Getting started

### Prerequisites

- **Java 21+** and **Maven** (or use the included `mvnw`/`mvnw.cmd` wrapper)
- **Node.js** and **npm**
- A local **MySQL** server
- API keys: [TMDB](https://www.themoviedb.org/settings/api), [OMDb](https://www.omdbapi.com/apikey.aspx) (optional), [Groq](https://console.groq.com/keys)
- SMTP credentials for password-reset email (e.g. [Brevo's free SMTP](https://www.brevo.com/free-smtp-server/))

### Backend setup

```bash
cd backend
copy src\main\resources\application-local.properties.example src\main\resources\application-local.properties
```

Fill in `application-local.properties` (gitignored) with:

| Variable | Purpose |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `JWT_SECRET` | Signs and verifies access tokens |
| `TMDB_API_KEY` | TMDB calls |
| `OMDB_API_KEY` | OMDb calls (optional) |
| `GROQ_API_KEY` | Groq calls |
| `BREVO_SMTP_HOST`, `BREVO_SMTP_USERNAME`, `BREVO_SMTP_PASSWORD` | Password-reset email |

`schema.sql` / `data.sql` run automatically on every startup and are idempotent (`IF NOT EXISTS` / `ON DUPLICATE KEY UPDATE`), so no manual migration step is needed — just make sure the MySQL server is running first.

```bash
.\mvnw.cmd spring-boot:run      # run the API on :8080
.\mvnw.cmd compile              # verify it builds
.\mvnw.cmd test                 # run tests
```

### Frontend setup

```bash
cd frontend
npm install
npm run dev      # dev server on :5173
```

Other frontend commands: `npm run build` (`tsc -b && vite build`), `npm run lint` (oxlint), `npm run preview`.

The backend allows CORS from `http://localhost:5173`. Once both are running, open `http://localhost:5173`.

## Project structure

```
Movie-Recommendation-System/
├── backend/          Spring Boot REST API (Java 21, Maven)
│   └── src/main/java/com/movierecommender/
│       ├── entity/       JPA entities, enums, converters
│       ├── repository/   Spring Data JPA repositories
│       └── config/       CORS, Security
├── frontend/         React 19 + Vite + TypeScript app
├── docs/             Product & technical specs (PRD, TRD, schema, API contracts, forms, UI/UX)
├── diagrams/         Interactive architecture/sequence/workflow/dataflow/lifecycle diagrams
├── Plan/             Build architecture plan and phase-by-phase to-do list
└── rules.md          Repo-specific working rules
```

## Error handling

Every error response uses one envelope shape:

```json
{ "error": { "code": "TMDB_UNREACHABLE", "message": "Couldn't reach the movie database" } }
```

Full status/code table is in the [TRD](docs/trd.md#error-handling).

## Contributing

Solo portfolio project — not currently accepting external contributions. Feel free to open an issue if you spot a bug.

## License

No open-source license has been applied yet; all rights reserved by the author pending a licensing decision.

## Author

[Nilanchal0107](https://github.com/Nilanchal0107)
