# Movie Recommender — Implementation To-Do List (v1)

Last updated: September 2026  
Status: In Progress / Tracking  
Architecture Plan: [Plan/plan.md](./plan.md)  
Specification Documents: [PRD](../docs/prd.md) · [TRD](../docs/trd.md) · [Database Schema](../docs/database-schema.md) · [API Contracts](../docs/api-contracts.md) · [Forms Spec](../docs/forms-spec.md) · [UI/UX Spec](../docs/ui-ux-spec.md) · [Wireframes](../docs/wireframes.md)

---

## Progress Overview

- [x] **Phase 1:** Environment & Project Scaffolding
- [x] **Phase 2:** Database Schema & Migration
- [x] **Phase 3:** Authentication, Security & User Profile
- [x] **Phase 4:** TMDB Integration & Local Title Caching
- [x] **Phase 5:** Matching Engine & Misery Algorithm
- [x] **Phase 6:** Groq LLM Concurrent Integration
- [x] **Phase 7:** Frontend Design System & Common UI
- [x] **Phase 8:** Frontend Flows 1–4 (Auth, Profile, Home, Session Setup)
- [x] **Phase 9:** Frontend Flows 5–7 (Handoff, Person Form, Finding Picks)
- [x] **Phase 10:** Frontend Flows 8–9 (Results & Title Detail)
- [~] **Phase 11:** End-to-End Integration, Edge Cases & Verification (5/6 items verified; keyboard navigation needs a live browser pass — see notes)

---

## Phase 1: Environment & Setup

### Backend (Spring Boot + Maven)
- [x] Verify Java installation (`java -version`, JDK 21+).
- [x] Verify Maven installation (`mvn -v`).
- [x] Initialize `/backend` as a Spring Boot 3.x project using Maven (`pom.xml`):
  - [x] Add `spring-boot-starter-web`
  - [x] Add `spring-boot-starter-security`
  - [x] Add `spring-boot-starter-data-jpa`
  - [x] Add `mysql-connector-j`
  - [x] Add `jjwt` / Nimbus JWT dependencies
  - [x] Add `spring-boot-starter-mail` (Brevo SMTP with local console fallback)
  - [x] Add `spring-boot-starter-validation`
  - [x] Add `spring-boot-starter-webflux` (WebClient for TMDB and Groq)
- [x] Include Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`) in `/backend`.
- [x] Create `/backend/src/main/resources/application.properties` and template `application-local.properties` (gitignored).
- [x] Configure Spring Security CORS filter for frontend dev origin (`http://localhost:5173`).

### Frontend (React + Vite)
- [x] Verify Node.js (`node -v`) and npm (`npm -v`) installation.
- [x] Initialize `/frontend` using Vite + React with TypeScript (`npm create vite@latest frontend -- --template react-ts`).
- [x] Configure Google Fonts (Bebas Neue & Inter) in `/frontend/index.html`.
- [x] Implement design tokens in `/frontend/src/index.css` for **Midnight Cinema** theme (`#0E0F13` background, `#1A1C23` surface, `#E5A00D` gold accent, score badges, focus rings).

---

## Phase 2: Database Schema & Entity Modelling
- [x] Create MySQL database `movie_recommender`.
- [x] Create database initialization script (`schema.sql`) for all 10 tables:
  - [x] `users`
  - [x] `user_profiles`
  - [x] `password_reset_tokens`
  - [x] `genres`
  - [x] `keywords`
  - [x] `titles`
  - [x] `sessions`
  - [x] `session_person_answers`
  - [x] `session_results`
  - [x] `session_result_person_scores`
- [x] Implement JPA Entities and AttributeConverters for JSON column mapping:
  - [x] `User.java`
  - [x] `UserProfile.java` (languages, favorite_genres, never_want_genres, loved_title_ids)
  - [x] `PasswordResetToken.java`
  - [x] `Genre.java`
  - [x] `Keyword.java` (flags: `is_intensity_marker`)
  - [x] `Title.java` (`certification_band` indexed column, `genre_ids`, `keyword_ids`, `cast` top 5)
  - [x] `Session.java`
  - [x] `SessionPersonAnswer.java` (moods, genres_up_for, hard_no_genres, languages_ok)
  - [x] `SessionResult.java`
  - [x] `SessionResultPersonScore.java`
- [x] Seed initial lookup tables:
  - [x] Marker intensity keywords: `graphic violence`, `gore`, `sexual violence`, `torture`
  - [x] Mood proxy keywords: `horror`, `romance` for TV series
  - [x] Standard TMDB movie and TV genres

---

## Phase 3: Auth & Profile
- [x] Build stateless `JwtTokenProvider` (24-hour expiration) and `JwtAuthenticationFilter`.
- [x] Build global `RestExceptionHandler` returning standardized error envelope: `{"error": {"code": "...", "message": "..."}}`.
- [x] Implement `POST /auth/signup`:
  - [x] Hash password using BCrypt.
  - [x] Check unique email (`409 EMAIL_ALREADY_REGISTERED`).
  - [x] Return signed JWT and user object (`201 Created`).
- [x] Implement `POST /auth/login`:
  - [x] Verify credentials (`401 INVALID_CREDENTIALS`).
  - [x] Return signed JWT and user object (`200 OK`).
- [x] Implement `POST /auth/forgot-password`:
  - [x] Generate single-use secure token with 30-min expiry.
  - [x] Dispatch email via `JavaMailSender` (or log link if SMTP not configured).
  - [x] Return uniform message to prevent email enumeration.
- [x] Implement `POST /auth/reset-password`:
  - [x] Verify token and expiry (`410 RESET_TOKEN_EXPIRED`, `400 RESET_TOKEN_INVALID`).
  - [x] Update password and mark token used.
- [x] Implement `GET /profile`:
  - [x] Return user profile merged with account display name and email.
- [x] Implement `PUT /profile`:
  - [x] Validate bounds (max 5 favorite genres, max 5 loved titles, non-empty languages).
  - [x] Upsert `user_profiles` row.

---

## Phase 4: External Integrations & Local Title Caching
- [x] Implement `TmdbClient` using WebClient with `TMDB_API_KEY`:
  - [x] Search multi / movies / series.
  - [x] Discover movies and series with genres, certifications, and languages.
  - [x] Detail endpoint with `append_to_response=release_dates,content_ratings,credits,keywords`.
- [x] Implement Indian certification hierarchy parser and US fallback:
  - [x] Map Indian ratings (`U`, `UA 7+`, `UA 13+` / `UA`, `UA 16+`, `A`) to internal bands (`U`, `UA7`, `UA13`, `UA16`, `A`).
  - [x] Map US fallback ratings (Movies: `G`, `PG`, `PG-13`, `R`/`NC-17`; TV: `TV-Y`, `TV-PG`, `TV-14`, `TV-MA`).
  - [x] Assign `null` / `"Not rated"` if neither is found.
- [x] Address Forms Spec Data Traps:
  - [x] *Runtime Trap:* Store parsed runtime locally and filter in SQL/memory rather than trusting TMDB Discover.
  - [x] *Series Age Trap:* Discover TV has no age filter; fetch content ratings and filter locally.
- [x] Implement `GET /genres` endpoint (cached from TMDB).
- [x] Implement `POST /titles/search`:
  - [x] Query TMDB by title query and media type.
  - [x] Immediately upsert returned titles with basic info (id, tmdbId, title, poster, year) into MySQL.
- [x] Implement `GET /titles/{id}`:
  - [x] Return complete title details, top 5 cast, ratings, and certifications.

---

## Phase 5: Matching Engine & Misery Algorithm
- [x] Build hard filter candidate query:
  - [x] Age limit: `certification_band <= youngestPersonAgeBand` (or unrated). *(Fixed a pre-existing bug: `certificationBand` is a String-converted enum, so JPQL `<=` compared lexicographically; `TitleRepository.findCandidates` now takes an explicit `List<CertificationBand>` of allowed bands computed by ordinal instead.)*
  - [x] Language: `original_language` in group's common intersection of `languages_ok`.
  - [x] Hard-no genres: title's `genre_ids` contains none of any attendee's `hard_no_genres` (matched by genre name, so it works across movie/series genre-id spaces for `Either` sessions).
  - [x] Q7 Intensity check:
    - If anyone selected `light`: exclude titles with any intensity marker keywords.
    - If anyone selected `intense`: require title to have at least one intensity marker keyword.
  - [x] Runtime filter: movie runtime bracket (hard cap) and series duration/status (finished vs ongoing, episode length) from `session.timeLimit`.
  - [x] Replenish candidate pool from TMDB Discover if local cached pool < 50 items (`TitleCacheService.discoverAndCache`).
- [x] Build per-person scoring function (0–100 scale) in `MatchingEngineService`:
  - [x] Genre match (Weight 35)
  - [x] Mood-to-genre/keyword match (Weight 25)
  - [x] Light / intense slider alignment (Weight 15) — always full marks for survivors since Q7 is enforced as a hard filter above.
  - [x] Similarity to `something_like_title_id`, falling back to the host's profile `loved_title_ids` when Q8 is skipped (Weight 15)
  - [x] Era preference match (Weight 10) — host only, from profile `era_preference` (no era field exists per-person in the API contract).
- [x] Implement solo mode:
  - [x] Automatically calculate age band from `current_year - birth_year` (`AgeBandMapper`), applied to `personIndex 0` regardless of group size.
  - [x] Pre-fill answers from user profile — n/a on the backend beyond the age-band override; the wizard pre-fill itself is a Phase 8/9 frontend concern.
- [x] Implement "Average without Misery" algorithm:
  - [x] Initial misery threshold = 40.
  - [x] Filter out candidates where `min(person_score) < threshold`.
  - [x] Calculate `group_score = avg(person_scores) + (vote_average * 0.5)`.
  - [x] If < 5 titles survive: retry with threshold 30; if still < 5, retry with threshold 20.
  - [x] Mark lowered-threshold titles with badge `compromise`.
  - [x] Select top 5 surviving candidates.
- [x] Implement Session Endpoints:
  - [x] `POST /sessions/start`: validate S1–S4 and create `sessions` record.
  - [x] `POST /sessions/{id}/submit`: persist all attendee Q1–Q8 answers, execute matching, save `session_results` & `session_result_person_scores`, return rank 1–5. `aiExplanation` is a programmatic placeholder one-liner until Phase 6 wires up `GroqClient`.
  - [x] `POST /sessions/{id}/more`: re-run matching excluding prior `title_id`s, save and return rank 6–10.
  - [x] `GET /sessions/{id}/results`: return existing session results (for page refresh).

Note: real Groq explanations are Phase 6's job; `aiExplanation` currently returns a generic templated line, which also happens to be exactly the `GROQ_UNREACHABLE` fallback behavior the TRD asks for.

---

## Phase 6: Groq LLM Concurrent Integration
- [x] Build `GroqClient` with API key and configurable model (`llama-3.3-70b-versatile`).
- [x] Construct prompt for each title containing title, genres, occasion, attendee names, and individual score highlights.
- [x] Dispatch all 5 Groq prompts in parallel (reactive `Flux.merge` over per-title `Mono<String>` calls, collected together).
- [x] Implement graceful fallback: if Groq is unreachable or times out (10s), each call individually degrades to the programmatic one-liner instead of failing the request.

---

## Phase 7: Frontend Design System & Components
- [x] Implement shared UI components in `/frontend/src/components`:
  - [x] `Button`: primary gold `#E5A00D`, secondary outline, loading state, disabled styling.
  - [x] `Chip`: multi-select and single-select chips with gold checkmarks and keyboard toggle.
  - [x] `TextInput`: accessible text input with floating label / error display.
  - [x] `Stepper`: `[ - ] count [ + ]` for group size (1 to 8).
  - [x] `Slider3Step`: 3-stop slider for Q7 (Light, Balanced, Intense).
  - [x] `TitleSearchPicker`: debounced search input with poster preview chips and remove button.
  - [x] `PersonScoreCircles`: colored attendee initials (`#3DD68C` green for 70+, `#F5A524` amber for 40–69).
  - [x] `FilmReelSpinner`: animated film-reel loading spinner.
  - [x] `Navbar`: title, user avatar/name dropdown, Edit profile link, Log out button.
- [x] Setup API client with token interceptor and standardized error handling.

This phase's checkbox was simply never ticked — all of the above were already built and in use by Phases 8–10 (`frontend/src/components/*.tsx`, `frontend/src/api/client.ts`); confirmed present during the Phase 11 pass.

---

## Phase 8: Frontend Screens 1 to 4
- [x] **Screen 1: Sign up / Log in / Password Reset (`/login`, `/signup`, `/forgot-password`, `/reset-password`):**
  - [x] Split screen layout: blurred poster backdrop on left, card on right.
  - [x] Sign up tab with password match validation.
  - [x] Log in tab with redirect to home/profile.
  - [x] Forgot password flow with "Check your inbox" screen.
  - [x] Reset password page with 30-min token parameter.
- [x] **Screen 2: Profile Setup Wizard (`/profile/setup`):**
  - [x] Progress bar for 3 steps.
  - [x] Step 1 (About You): pre-filled display name (read-only — no backend endpoint exists to rename the account), birth year dropdown (P2).
  - [x] Step 2 (Languages): P4 languages chips + free-text add, P5 subtitles Yes/No toggle.
  - [x] Step 3 (Taste): P6 favorite genres (up to 5), P7 hard-no genres, P8 loved titles search picker (up to 5 optional, movie/series toggle), P9 era preference 3-step choice (`Slider3Step` generalized to take arbitrary steps, reused from Phase 7).
  - [x] State preservation on Back / Next; prefills from `GET /profile` when reopened via "Edit profile".
- [x] **Screen 3: Home (`/`):**
  - [x] Greeting header ("Hi [Name]").
  - [x] Prominent gold CTA: "Find something to watch".
  - [x] Profile summary card with "Edit" button.
- [x] **Screen 4: Session Setup (`/session/new`):**
  - [x] S1: 3 cards (Movie, Series, Either).
  - [x] S2: Stepper (1 to 8 viewers).
  - [x] S3: Dynamic time limit chips (S3a movie runtimes vs S3b series duration; "Either" shows both).
  - [x] S4: Occasion chips (Friends hangout, Date night, Family time, Solo chill), deselectable.
  - [x] "Start" button navigating to Handoff (group) or Person Form (solo) — routes are stubbed with a "coming soon" placeholder until Phase 9 builds those screens.

Also added: `react-router-dom` for client routing, an `AuthContext` (in-memory token/user, matching the TRD's "no localStorage" rule), and `api/auth.ts` · `api/profile.ts` · `api/genres.ts` · `api/sessions.ts` API modules. All verified against the running backend with `curl` (signup, login, `GET/PUT /profile`, `GET /genres`, `POST /sessions/start`) — request/response shapes match exactly.

---

## Phase 9: Frontend Screens 5 to 7
- [x] **Screen 5: Handoff Screen (`/session/:id/handoff/:index`):**
  - [x] Centered full-screen dark privacy wall.
  - [x] "Pass the laptop to person X of N".
  - [x] Numbered progress dots.
  - [x] Gold "I'm ready" button (autofocused, so Enter triggers it).
  - [x] "Go back" link to allow fixing previous person's answers.
- [x] **Screen 6: Person's Form (`/session/:id/person/:index`):**
  - [x] Host goes first (`personIndex = 0`), pre-filled with profile answers (name, favourite/never-want genres capped to Q4's 3, languages).
  - [x] Solo mode: skip Q1 (Name) and Q2 (Age Band).
  - [x] Questions Q1 to Q8 with constraints (Q3 max 2 chips, Q4 max 3 chips, Q7 slider defaulting to Balanced, Q8 title search picker + Skip).
  - [x] Client validation: red error text and auto-scroll to the first missing required section (name, age band, mood, or language).
  - [x] Answers held only in a new `SessionFlowContext` (frontend memory), never sent until the last person's "Done" triggers `/submit`.
- [x] **Screen 7: Finding Picks (`/session/:id/loading`):**
  - [x] Centered message: "Matching N people's answers...".
  - [x] Film-reel loading animation (reused `FilmReelSpinner` from Phase 7).
  - [x] After 10s: display "Still working...".
  - [x] Error state with Retry CTA on failure (re-runs the same `/submit` call).
  - [x] Empty state: "Nothing fits everyone tonight" with a generic loosen-filters CTA back to Session setup — the API doesn't return which filter was most restrictive, so that specific detail from the wireframe isn't shown.

Also added: `SessionFlowContext` (mirrors `AuthContext`'s in-memory-only pattern) to carry the session's `mediaType`/`groupSize` and each person's draft answers across the handoff loop without persisting anything to storage — consistent with the "never sent until everyone's done" rule and the app's existing no-persistence-on-refresh behavior. Extracted `groupGenresByName` (genre dedup-by-name-across-movie/tv) and `POPULAR_LANGUAGES` out of `ProfileSetupPage` into `utils/genres.ts` and `utils/languages.ts` so the person form could reuse them instead of duplicating. Verified with `tsc -b && vite build` (clean) and `oxlint` (clean, pre-existing-style warnings only), plus a live signup → profile → session start → `/submit` round trip against the running backend — the request was accepted and reached the TMDB call; this sandbox has no outbound internet, so the full match itself couldn't be exercised (`TMDB_UNREACHABLE`, which is exactly the error state Screen 7 is built to show).

---

## Phase 10: Frontend Screens 8 to 9
- [x] **Screen 8: Results: Top 5 (`/session/:id/results`):**
  - [x] Header with session summary and attendee names.
  - [x] 5 recommendation cards in a single row:
    - [x] 2:3 poster image.
    - [x] Title, release year, runtime, certificate.
    - [x] Gold group match percentage.
    - [x] Colored score circle per attendee.
    - [x] 2-line AI generated explanation.
    - [x] Badges (`Compromise pick`, `Not rated`).
  - [x] "Show more" button fetching and displaying rank 6–10 in a second row.
  - [x] "Change answers" and "Start over" buttons — "Change answers" returns to `/session/new`; a prefilled setup form isn't wired up (`SessionSetupPage` doesn't read `SessionFlowContext` on mount), so answers aren't literally "kept" yet, same as the existing empty-state CTA in `FindingPicksPage`.
- [x] **Screen 9: Title Detail (`/session/:id/title/:titleId`):**
  - [x] Wide backdrop hero image.
  - [x] Poster, title, year, certification, runtime, overview, genres.
  - [x] Cast list with headshot photos (however many the backend returns — capped at 5 server-side per Phase 4).
  - [x] "Why this pick" block: full AI explanation + one line per attendee. The backend doesn't return a genre/mood breakdown string per person (only a numeric score), so each line reads "Name: NN% match" rather than the wireframe's "matches Thrill and Hindi" — that phrasing would need a new backend field.
  - [x] "Back to results" button (Esc key support) — both use `navigate(-1)`, which restores the Results page's already-fetched state instead of refetching.

Also added: `getMoreResults`/`getSessionResults` to `api/sessions.ts` and `getTitleDetail` to `api/titles.ts` (endpoints existed on the backend since Phase 5 but had no frontend client yet). `SessionSetupInfo` (SessionFlowContext) gained a `timeLimit` field so the Results header can show "Movie · 90–120 min" on the fresh-submit path; `FindingPicksPage` now passes the `/submit` response via router state instead of clearing the session context immediately, so Results doesn't need a redundant fetch on the common path. Both new pages still fetch from `GET /sessions/:id/results` standalone (page refresh, direct link) rather than depending on that state. Verified with `tsc -b && vite build` (clean) and `oxlint` (clean, only pre-existing-style warnings shared with Phase 9's pages).

---

## Phase 11: Testing & Refinement
- [ ] **Keyboard Navigation:** Validate complete session walkthrough using Tab, Enter, Space, and Arrow keys. *Not verified live — this sandbox has no browser automation tool. Code-level check only: `onKeyDown`/`autoFocus`/`tabIndex`/ARIA hooks exist in `Chip`, `Stepper`, `Slider3Step`, `HandoffPage` (autofocused "I'm ready"), `TitleDetailPage` (Esc → back). Needs a real-browser pass (manual or a Playwright/browser-tool session) before shipping.*
- [x] **Solo Mode Flow:** Validated end-to-end against the real running backend (`POST /sessions/start` → `/submit` → `GET /results` → `/more`): single-person scoring, refresh-safe re-fetch, and rank 6–10 exclude rank 1–5.
- [x] **Group Mode Flow:** Validated a 3-person session with an adult + a 13–15 minor + conflicting genre/mood preferences: certification-band filtering correctly capped all results to the minor's ceiling (no `A`-band titles leaked in), confirmed against a control session (no minor) where `A`-band titles were eligible.
- [x] **Compromise Logic:** Forced genre/mood/era mismatches down to a uniform sub-40 score and confirmed the engine retries at threshold 30 and marks survivors `badge: "compromise"` — the 40→30→20 ladder works as designed.
- [x] **Resilience:** TMDB is genuinely unreachable from this dev sandbox (confirmed: `curl` to `api.themoviedb.org` times out while other hosts succeed) — every `/submit` call against an empty/undersized local cache correctly returned `502 TMDB_UNREACHABLE` per the error envelope, so that fallback path got real (not simulated) coverage. Groq fallback also got real coverage, but for an unplanned reason — see finding below.
- [x] **Production Build Check:** `npm run build` (tsc -b && vite build) is clean. `mvn clean package` **fails on JDK 26** (`ExceptionInInitializerError: TypeTag :: UNKNOWN` — a Lombok/very-new-javac incompatibility exposed once `clean` removes the stale cached classes the dev server had been running on) but **succeeds cleanly, tests included**, on JDK 23. No JDK 21 (the version this project targets, per `pom.xml` and `CLAUDE.md`) is installed on this machine at all — only 8, 17, 23, and 26. Build with JDK 17–23 until Lombok ships JDK 26 support, or install a JDK 21 to match the pinned target exactly.

**Findings from this pass** (surfaced by testing, not fixed blindly — these are either judgment calls or pre-existing documented design, so flagged instead of silently changed):
1. **Fixed:** `ResultsPage.tsx`'s "Show more" handler was concatenating `/more`'s response onto the existing 5 results. The backend's `/more` (`SessionService.more()` → `buildResponse()`) already returns the *full* accumulated list (ranks 1–10, same contract as `GET /results`) — so appending it duplicated the first 5 cards. Now replaces `data` with the response instead of merging.
2. **Not fixed, flagged:** `MatchingEngineService.passesGenreAndIntensity` applies Q7 as a **hard** filter unioned across every attendee: `anyLight` excludes any title with an intensity-marker keyword, `anyIntense` requires one. If a group has both a "Light" and an "Intense" answer (plausible in any real 3+ person group), these two conditions are mutually exclusive for every title — the session **always** returns zero results, regardless of misery threshold. This is the documented Phase 5 design (see this file's git history / the bullet above), not an accident, but it means "conflicting tastes" groups can hard-fail in a way the misery-threshold/compromise mechanism can't rescue. Worth a product call: soften Q7 into per-person scoring like the other weighted signals, or keep it a hard filter and surface a clearer "your group's intensity picks cancel out" message instead of the generic empty state.
3. **Not fixed, flagged:** `SessionService.resolveBadge()` gives `NOT_RATED` precedence over `COMPROMISE` — an unrated compromise pick shows "Not rated" and silently drops the "Compromise pick" signal, since `ResultDto.badge` is a single enum value, not a set. Minor, but worth knowing before relying on the compromise badge for QA.
4. **Not fixed, flagged — likely production-impacting:** the configured Groq model (`app.groq.model` / `GROQ_MODEL`, defaulting to `llama-3.3-70b-versatile` in `application.properties`) returns `404 model_not_found` from Groq's real API right now — confirmed with a direct authenticated `curl` against `api.groq.com/openai/v1/chat/completions` using the project's real key. This isn't a sandbox artifact: it reproduces with real network access and a real key, so **every deployment using the current default is silently degrading to the generic templated explanation** (the graceful-fallback path masks it well, which is why it wasn't obvious). Current key's `/v1/models` no longer lists any `llama-3.3-*` id; available chat-capable models include `openai/gpt-oss-120b` and `openai/gpt-oss-20b`. Needs a deliberate pick, not a silent swap — flagging for a decision.
