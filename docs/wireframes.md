# Movie Recommender — Wireframes

Last updated: 27 Sep 2026 · Related docs: [PRD](./prd.md) · [UI/UX Spec](./ui-ux-spec.md) · [Forms Spec](./forms-spec.md) · [TRD](./trd.md) · [Database Schema](./database-schema.md) · [API Contracts](./api-contracts.md)

Rough box layouts for all 9 v1 screens, in flow order. These are structural sketches (what sits where), not the final visual design — colors, type and spacing are in the UI/UX Spec's Theme options and Shared components sections.

## Screen flow (recap)

```mermaid
flowchart LR
    A[1 . Sign up / Log in] --> B[2 . Profile setup]
    B --> C[3 . Home]
    C --> D[4 . Session setup]
    D --> E[5 . Handoff]
    E --> F[6 . Person's form]
    F --> G{More people?}
    G -- yes --> E
    G -- no --> H[7 . Finding picks]
    H --> I[8 . Results: top 5]
    I -- open a card --> J[9 . Title detail]
```

## 1. Sign up / Log in

```
┌──────────────────────────────────────────────────────────────┐
│                                                                │
│   ░░░░░░░░░░░░░░░░░░░░░░   ┌──────────────────────────────┐  │
│   ░░  blurred poster   ░░   │  [ Sign up ]   Log in         │  │
│   ░░    collage        ░░   │ ────────────                  │  │
│   ░░░░░░░░░░░░░░░░░░░░░░   │                                │  │
│                             │  Name      [____________]     │  │
│      MOVIE                 │  Email     [____________]     │  │
│      RECOMMENDER            │  Password  [____________]     │  │
│      (Bebas Neue)            │  Confirm   [____________]     │  │
│                             │                                │  │
│                             │        [   Sign up   ]        │  │
│                             │                                │  │
│                             └──────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘

  Log in tab:
  ┌──────────────────────────────┐
  │  Sign up   [ Log in ]         │
  │ ─────────                     │
  │  Email     [____________]     │
  │  Password  [____________]     │
  │            Forgot password?   │
  │        [    Log in    ]       │
  └──────────────────────────────┘

  Forgot password (own page):
  ┌──────────────────────────────┐
  │  ← Back to log in              │
  │                                │
  │  Reset your password           │
  │  Email     [____________]      │
  │        [  Send reset link  ]   │
  └──────────────────────────────┘
```

## 2. Profile setup (first login only)

```
┌──────────────────────────────────────────────────────────────┐
│  ●───────○───────○     Step 1 of 3: About you                │
│                                                                │
│         Display name (from sign up, editable)                 │
│         [ Amey                            ]                   │
│                                                                │
│         Birth year                                            │
│         [ 1998 ▾ ]                                            │
│                                                                │
│                                                                │
│                                    [ Back ]   [   Next   ]     │
└──────────────────────────────────────────────────────────────┘

  Step 2 — Languages:
┌──────────────────────────────────────────────────────────────┐
│  ○───────●───────○     Step 2 of 3: Languages                │
│                                                                │
│         Languages you understand                              │
│         [Hindi ✓] [English ✓] [Tamil] [Telugu] [+ search]    │
│                                                                │
│         Okay with subtitles?     ( Yes )  ( No )               │
│                                                                │
│                                    [ Back ]   [   Next   ]     │
└──────────────────────────────────────────────────────────────┘

  Step 3 — Taste:
┌──────────────────────────────────────────────────────────────┐
│  ○───────○───────●     Step 3 of 3: Taste                    │
│                                                                │
│         Favourite genres (up to 5)                             │
│         [Comedy ✓] [Thriller ✓] [Drama] [Sci-Fi] [Horror]     │
│                                                                │
│         Genres you never want                                 │
│         [Horror ✓] [+ add]                                    │
│                                                                │
│         Titles you loved (optional)                            │
│         [ 🔍 Search movies/series...            ]             │
│         [poster] [poster] [poster]   +Add more                │
│                                                                │
│         Newer or classic?                                     │
│         ( Mostly newer )  ( Any era )  ( Love classics )       │
│                                                                │
│                                    [ Back ]   [  Finish  ]     │
└──────────────────────────────────────────────────────────────┘
```

## 3. Home

```
┌──────────────────────────────────────────────────────────────┐
│  MOVIE RECOMMENDER                          [ Amey ▾ ]        │
│                                              Edit profile      │
│                                              Log out           │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│                       Hi Amey                                 │
│                                                                │
│              ┌────────────────────────────┐                   │
│              │  Find something to watch   │  ← gold button    │
│              └────────────────────────────┘                   │
│                                                                │
│        ┌──────────────────────────────────────┐               │
│        │ Your profile                          │               │
│        │ Languages: Hindi, English              │               │
│        │ Favourites: Comedy, Thriller           │               │
│        │                                 [Edit] │               │
│        └──────────────────────────────────────┘               │
│                                                                │
└──────────────────────────────────────────────────────────────┘
```

## 4. Session setup

```
┌──────────────────────────────────────────────────────────────┐
│  ← Cancel                        Let's set up tonight          │
│                                                                │
│  What are we watching?                                         │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐                  │
│  │  Movie    │  │  Series   │  │  Either   │                  │
│  └───────────┘  └───────────┘  └───────────┘                  │
│       ▲ selected                                                │
│                                                                │
│  How many people are watching?                                 │
│         [ − ]        4        [ + ]                            │
│                                                                │
│  How much time?                                                 │
│  [Under 90 min] [90–120 min ✓] [2–2.5 hrs] [No limit]          │
│                                                                │
│  What's the occasion? (optional)                                │
│  [Friends hangout ✓] [Date night] [Family time] [Solo chill]   │
│                                                                │
│                                          [     Start     ]     │
└──────────────────────────────────────────────────────────────┘
```

## 5. Handoff screen

```
┌──────────────────────────────────────────────────────────────┐
│                                                                │
│                                                                │
│                                                                │
│               Pass the laptop to person 2 of 4                │
│                                                                │
│                   ●  ○  ○  ○                                   │
│                (done)(next)                                    │
│                                                                │
│                    ┌───────────────┐                           │
│                    │  I'm ready    │                           │
│                    └───────────────┘                           │
│                                                                │
│                       Go back                                  │
│                                                                │
│                                                                │
└──────────────────────────────────────────────────────────────┘
     full-screen · dark background · everything centred
```

## 6. Person's form

```
┌──────────────────────────────────────────────────────────────┐
│  Person 2 of 4                     ▓▓▓▓▓▓▓░░░░░░░░░ (progress)│
│                                                                │
│  Your name                                                     │
│  [___________________________]                                 │
│                                                                │
│  Your age group                                                 │
│  [Under 7] [7–12] [13–15] [16–17 ✓] [18+]                      │
│                                                                │
│  What's your mood tonight? (pick 1–2)                           │
│  [Laugh ✓] [Thrill] [Feel something] [Think] [Get scared]      │
│  [Comfort] [Adventure] [Romance]                                │
│                                                                │
│  Genres you're up for (up to 3)                                 │
│  [Comedy ✓] [Action] [Drama] [Sci-Fi] ...                       │
│                                                                │
│  Hard no tonight                                                │
│  [Horror ✓]                                                     │
│                                                                │
│  Languages you're okay with                                     │
│  [Hindi ✓] [English ✓]                                          │
│                                                                │
│  Light or intense?                                              │
│  Light ────●──── Intense   (set to Balanced)                    │
│                                                                │
│  Something like... (optional)                                   │
│  [ 🔍 Search...                    ]              Skip →        │
│                                                                │
│                                          [     Done     ]      │
└──────────────────────────────────────────────────────────────┘
   alone: name + age group rows are hidden, rest pre-filled
```

## 7. Finding picks

```
┌──────────────────────────────────────────────────────────────┐
│                                                                │
│                                                                │
│                     ⟳  (film-reel spinner)                     │
│                                                                │
│              Matching 4 people's answers…                     │
│                                                                │
│        (holds here until scoring + all 5 AI                   │
│         explanations are ready, then jumps                    │
│         straight to a fully-formed Results screen)            │
│                                                                │
└──────────────────────────────────────────────────────────────┘

  No results:
┌──────────────────────────────────────────────────────────────┐
│                                                                │
│              Nothing fits everyone tonight                     │
│         Most titles were cut by: the 90-minute limit           │
│                                                                │
│                   [   Loosen this filter   ]                   │
└──────────────────────────────────────────────────────────────┘
```

## 8. Results: top 5

```
┌──────────────────────────────────────────────────────────────┐
│  Top 5 for Rahul, Priya, Sam + 1                               │
│  Movie · 90–120 min                                            │
│                                                                │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐        │
│  │[poster]│ │[poster]│ │[poster]│ │[poster]│ │[poster]│        │
│  │Title A │ │Title B │ │Title C │ │Title D │ │Title E │        │
│  │2023·105│ │2021·98 │ │2024·112│ │2019·100│ │2022·95 │        │
│  │  min·UA│ │min·U   │ │min·UA13│ │min·A   │ │min·UA7 │        │
│  │  92% ★ │ │  87% ★ │ │  81% ★ │ │  76% ★ │ │  71% ★ │        │
│  │R P S T │ │R P S T │ │R P S T │ │R P S T │ │R P S T │        │
│  │●●●●    │ │●●●●    │ │●●●○    │ │●●○○    │ │●○●●    │        │
│  │"Fits   │ │"Everyone│ │Compro- │ │Not     │ │"Great  │        │
│  │ every-  │ │ likes  │ │mise    │ │rated   │ │ for the│        │
│  │ one's..│ │ thrill.│ │ pick   │ │        │ │ mood.."│        │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘        │
│                                                                │
│                        [    Show more    ]                     │
│                                                                │
│   [ Change answers ]                        [ Start over ]     │
└──────────────────────────────────────────────────────────────┘
   R P S T = one initial circle per person, green (70+) / amber (40–69)
```

## 9. Title detail

```
┌──────────────────────────────────────────────────────────────┐
│  ← Back to results                                             │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │
│  ░░░░░░░░░░░░░░ wide backdrop image ░░░░░░░░░░░░░░░░░░░░░░░  │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │
│                                                                │
│  ┌─────────┐   Title A                                         │
│  │         │   2023 · UA 13+ · 105 min                          │
│  │ poster  │   Comedy, Drama                                   │
│  │         │                                                  │
│  │         │   Overview: a short synopsis of the film goes     │
│  └─────────┘   here, two or three lines long...                │
│                                                                │
│                Cast                                             │
│                [pic] [pic] [pic] [pic] [pic]                    │
│                Name  Name  Name  Name  Name                     │
│                                                                │
│  ┌──────────────────────────────────────────────────────┐     │
│  │ Why this pick                                          │     │
│  │ Full AI explanation goes here, a few sentences...       │     │
│  │                                                          │     │
│  │ Rahul: matches Thrill and Hindi                          │     │
│  │ Priya: matches Comedy mood                                │     │
│  │ Sam: light/intense preference matched                    │     │
│  └──────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
```
