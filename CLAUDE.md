# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Movie Recommender — a group movie-night picker. One host starts a session (what to watch, group size, time, occasion), each person in the room answers a short private form on the same device, and the backend returns 5 titles the whole group can agree on with a plain-language reason for each. Solo portfolio project, v1 in progress.

Full specs live in `docs/`: [prd.md](docs/prd.md) (product scope/versions), [trd.md](docs/trd.md) (architecture, API, matching engine — read this first for anything backend), [database-schema.md](docs/database-schema.md), [api-contracts.md](docs/api-contracts.md), [forms-spec.md](docs/forms-spec.md) (per-question logic, how answers combine for a group), [ui-ux-spec.md](docs/ui-ux-spec.md), [wireframes.md](docs/wireframes.md). Build tracking is in `Plan/plan.md` and `Plan/to-do.md` (phase checklist — check this to see what's actually implemented vs. still spec-only).

## Git operations rule (strict, from [rules.md](rules.md))

Do not run any git command that writes or mutates repo state — no `add`, `commit`, `push`, `pull`, `checkout`, `reset`, `revert`, `merge`, `rebase`, `stash`, `rm`, `clean`, or branch create/delete. Only `git status` and `git log` are allowed. When a phase or feature is complete, give the user the exact `git add`/`git commit` commands to run themselves instead of running them.

## Commands

Backend (`/backend`, Spring Boot 3.3 + Java 21, Maven wrapper):
```
.\mvnw.cmd spring-boot:run      # run the API on :8080
.\mvnw.cmd compile               # verify it builds
.\mvnw.cmd test                  # run tests
.\mvnw.cmd test -Dtest=ClassName # run a single test class
```
Requires a local MySQL running and `backend/src/main/resources/application-local.properties` (copy from `application-local.properties.example`, gitignored) with `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`, `JWT_SECRET`, `TMDB_API_KEY`, `OMDB_API_KEY`, `GROQ_API_KEY`, `BREVO_SMTP_*`. `schema.sql`/`data.sql` run automatically on every startup (idempotent — `IF NOT EXISTS` / `ON DUPLICATE KEY UPDATE`).

Frontend (`/frontend`, React 19 + Vite + TypeScript):
```
npm run dev      # dev server on :5173
npm run build    # tsc -b && vite build
npm run lint     # oxlint
npm run preview
```

## Architecture

Monorepo, two independent apps: `/backend` (Spring Boot REST API) and `/frontend` (React, no fixed pixel widths, desktop-first). The frontend never calls TMDB/OMDb/Groq directly — every external call is proxied through the backend so API keys stay server-side. Auth is stateless JWT (24h access token, `Authorization: Bearer`, validated by a Spring Security filter on everything except `/auth/**`); the frontend keeps the token in memory, not localStorage.

Core request flow (see [trd.md](docs/trd.md) for the full sequence diagram): the host calls `POST /sessions/start`, then after the handoff loop (one form per person, held in frontend state only, never sent until everyone's done) `POST /sessions/{id}/submit` does the whole match in one round trip — hard SQL filters against the cached `titles` table (age rating, language, genre, runtime), then a per-person scoring function in Java (genre 35 / mood 25 / light-intense 15 / similarity 15 / era 10, deliberately not in SQL because the weights are expected to change), then a misery-threshold retry loop (40 → 30 → 20), then one Groq call per top-5 title run concurrently for the plain-language explanations, awaited together before the single response. `POST /sessions/{id}/more` repeats this for ranks 6–10, excluding titles already returned — nothing beyond what's been asked for is ever scored or explained.

TMDB/OMDb responses are cached in MySQL (`titles` table) rather than fetched per-request; `/titles/search` upserts a minimal row immediately (title/poster/year) so it can be referenced by ID right away, with full detail (cast, keywords, certification) fetched lazily when a title actually becomes a match candidate or its detail page opens.

Backend package layout so far (`backend/src/main/java/com/movierecommender/`): `entity/` (JPA entities) + `entity/enums/` + `entity/converter/` (JSON-column `AttributeConverter`s for list/array fields) + `entity/model/` (embeddable value types like `CastMember`), `repository/` (Spring Data JPA), `config/` (CORS, Security). Per `Plan/to-do.md`, phases 1–6 (scaffolding, schema, auth, TMDB integration, matching engine, Groq explanations) are done; the entire frontend UI (phases 7–11) is not yet built — check that file before assuming a component exists.

All error responses use one envelope shape: `{ "error": { "code": "...", "message": "..." } }` — see [trd.md](docs/trd.md#error-handling) for the status/code table.
