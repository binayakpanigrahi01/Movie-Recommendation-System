# Movie Recommender — Forms Spec

Last updated: 27 Sep 2026 · Related docs: [PRD](./prd.md) · [UI/UX Spec](./ui-ux-spec.md) · [Wireframes](./wireframes.md) · [TRD](./trd.md) · [Database Schema](./database-schema.md) · [API Contracts](./api-contracts.md)

## How the forms work

There are three forms, and every answer in them does exactly one of three jobs.

| Form | Who answers | When | Why it exists |
| --- | --- | --- | --- |
| Profile | The host (account owner) | Once, at sign-up | Stable facts that rarely change, so they are never asked twice |
| Session setup | The host | Every session, once | Facts shared by the whole group: what to watch, how long, how many people |
| Per-person | Each viewer, laptop passed around | Every session, once per person | Tonight's mood and limits, which differ person to person |

When watching alone, the per-person form skips name and age (both come from the profile). Group size is 1 to 8 people.

The three jobs an answer can do:

- **Hard filter:** removes titles completely (a 10-year-old in the group removes 18+ titles).
- **Score:** adds or takes away points, so good fits rise and poor fits sink.
- **AI context:** not used for matching, only handed to Groq so the explanation sounds personal.

## Profile questions (once, at sign-up)

Eight questions, asked once; they also pre-fill the host's own per-person form each session, so the host only changes what differs tonight.

| # | Question | Input | Options | Job |
| --- | --- | --- | --- | --- |
| P2 | Birth year | Dropdown | Year list | Hard filter (age rating when watching alone) |
| P4 | Languages you understand | Multi-select chips + search | Popular chips on top, search for any other | Hard filter |
| P5 | Okay with subtitles? | Yes / No toggle | Yes, No | Score (widens allowed languages) |
| P6 | Favourite genres | Chips, pick up to 5 | TMDB genre list | Score |
| P7 | Genres you never want | Chips, any number | TMDB genre list | Hard filter |
| P8 | Titles you loved (optional, up to 5) | Search picker (movies and series) | Any TMDB title | Score (finds similar titles) |
| P9 | Newer or classic? | 3-step choice | Mostly newer, Any era, Love classics | Score |

There's no P1 or P3: the display name was already collected at sign-up (shown pre-filled and editable at the top of the wizard, not asked again), and the app is India-only, so the country is fixed and age ratings always use Indian certificates.

**Why P8 matters:** a brand-new user has no ratings yet (the "cold start" problem). Titles they already loved are the fastest honest signal, and TMDB can return similar titles for each one.

## Session setup (host, once per session)

Four quick questions shared by the whole group, answered before the laptop is passed around.

| # | Question | Input | Options | Job |
| --- | --- | --- | --- | --- |
| S1 | What are we watching? | 3 cards | Movie, Series, Either | Hard filter (which TMDB list to search) |
| S2 | How many people are watching? | Stepper | 1 to 8 | Sets how many per-person forms follow |
| S3a | How much time? (movie) | 4 chips | Under 90 min, 90–120 min, 2–2.5 hrs, No limit | Hard filter (runtime) |
| S3b | How much time? (series) | 3 chips | An episode or two, A binge session, A new show to follow for weeks | Score (episode length, finished vs ongoing shows) |
| S4 | What's the occasion? | 4 chips, optional | Friends hangout, Date night, Family time, Solo chill | AI context |

S3 shows the movie options, the series options, or both, depending on S1.

## Per-person questions (each viewer)

Eight questions, almost all clicks, aimed at under 60 seconds per person; Q1 and Q2 are skipped when watching alone.

| # | Question | Input | Options | Job |
| --- | --- | --- | --- | --- |
| Q1 | Your name | Text | Free text | AI context ("Rahul gets his comedy") |
| Q2 | Your age group | 5 chips | Under 7, 7–12, 13–15, 16–17, 18+ | Hard filter (youngest band sets the age limit) |
| Q3 | What's your mood tonight? | Chips, pick 1–2 | Laugh, Thrill, Feel something, Think, Get scared, Comfort, Adventure, Romance | Score (via the mood map below) |
| Q4 | Genres you're up for | Chips, pick up to 3 | TMDB genre list | Score |
| Q5 | Hard no tonight | Chips, any number | TMDB genre list | Hard filter |
| Q6 | Languages you're okay with | Multi-select chips | Pre-filled from profile for the host | Hard filter (a title's language must be okay for everyone) |
| Q7 | Light or intense? | 3-step slider | Light, Balanced, Intense | Score |
| Q8 | Something like… (optional) | Search picker, skippable | Any TMDB title | Score (similar titles) |

Q3 and Q4 overlap on purpose: mood is how people naturally talk ("I want to laugh"), genres are for people who know exactly what they want.

**Age bands and Indian ratings.** Each band allows titles up to one certificate; the youngest person's band applies to the whole group.

| Age band | Highest certificate allowed |
| --- | --- |
| Under 7 | U |
| 7–12 | UA 7+ |
| 13–15 | UA 13+ |
| 16–17 | UA 16+ |
| 18+ | A (everything) |

The CBFC added UA 7+, UA 13+ and UA 16+ in November 2024 ([Wikipedia](https://en.wikipedia.org/wiki/Central_Board_of_Film_Certification)). Older films carry plain "UA" (parental guidance under 12), treated here as UA 13+. Indian streaming series use the same five categories ([TMDB forum](https://www.themoviedb.org/talk/63b7df66f44f2700a020d6f4)).

**No Indian certificate on TMDB?** Fall back to the US rating, mapped to the stricter side when it sits between two bands.

| US movie rating | US TV rating | Treated as |
| --- | --- | --- |
| G | TV-Y, TV-G | U |
| PG | TV-Y7, TV-PG | UA 7+ |
| PG-13 | — | UA 13+ |
| — | TV-14 | UA 16+ |
| R, NC-17 | TV-MA | A |

**No rating in either country?** The title is still shown, with a "Not rated" badge on its card so the group can judge for themselves.

**Solo mode (no age chip to click).** The profile only stores a birth year, not an age band. At request time, the backend computes `current_year − birth_year` and maps it onto the same five bands above (Under 7 / 7–12 / 13–15 / 16–17 / 18+), then applies the certification rule exactly as it would for a group.

## Keyword resolution

Q7 (light/intense) and two of the mood proxies for series need TMDB *keywords*, not genres — resolved once by name via TMDB's keyword search and cached, the same pattern as `genres`.

**Horror and Romance (series moods).** TMDB's own contributor guidance says to use genre-shaped concepts as keywords for TV when no dedicated genre exists: *"Feel free to add any 'missing' genre as a keyword: Horror, Romance, History, Music and Thriller"* ([TMDB TV Bible](https://www.themoviedb.org/bible/tv/59f73eb49251416e71000024)). This confirms the mood map above: search for the keyword named "horror" (or "romance"), cache its ID, and use it with `with_keywords` alongside the Mystery (or Drama/Comedy) genre.

**Light vs. Intense (Q7).** There's no single TMDB keyword meaning "light." Instead, a small set of **intensity marker** keywords is resolved and flagged once: `graphic violence`, `gore`, `sexual violence`, `torture` — an established tagging convention for flagging harsh content ([example keyword frequency](https://www.imdb.com/search/title/?keywords=graphic-violence&explore=keywords&ref_=tt_stry_kw)). The three Q7 positions then become:

| Q7 answer | TMDB filter |
| --- | --- |
| Light | `without_keywords` = all 4 marker IDs (title must carry none of them) |
| Balanced | no keyword filter — mood and genre carry the weight instead |
| Intense | `with_keywords` = all 4 marker IDs, pipe-separated (title must carry at least one) |

## Mood-to-genre map

Each mood chip turns into a set of TMDB genres; movies and series use different genre lists, so each mood has two columns.

| Mood | Movie genres | Series genres |
| --- | --- | --- |
| Laugh | Comedy, Animation | Comedy, Animation |
| Thrill | Action, Thriller, Crime | Action & Adventure, Crime |
| Feel something | Drama, Romance | Drama |
| Think | Mystery, Science Fiction, Documentary | Mystery, Sci-Fi & Fantasy, Documentary |
| Get scared | Horror, Thriller | Mystery + "horror" keyword |
| Comfort | Comedy, Family, Animation | Comedy, Family |
| Adventure | Adventure, Fantasy, Science Fiction | Action & Adventure, Sci-Fi & Fantasy |
| Romance | Romance, Comedy | Drama, Comedy + "romance" keyword |

TMDB's series genre list has no Horror or Romance genre, so those two moods use keywords for series. Genre IDs come from TMDB's genre list endpoints and are saved in MySQL once.

## How answers combine for a group

The group score uses "average without misery": a title is dropped if anyone would dislike it, and the rest are ranked by the group's average happiness. This matches the "nobody is unhappy" goal.

**Why not the simpler rules?** Research on group recommenders compares several ways to merge people's scores ([intro](https://towardsdatascience.com/an-introduction-to-group-recommender-systems-8f942a06db56/)):

- **Average:** one person can hate the pick while the others' love hides it.
- **Least misery** (rank by the unhappiest person's score): fair, but one person's mild dislike can sink a title everyone else loves, and a person can game it by rating everything very low ([Masthoff](https://www.researchgate.net/publication/227132202_Group_Recommender_Systems_Combining_Individual_Models)).
- **Average without misery:** removes titles below a dislike threshold for anyone, then averages. It protects each person without letting one person dominate.

**The steps, for every candidate title:**

1. **Hard filters.** Remove titles that fail any person's limits: age rating above the youngest person, anyone's hard-no genre, a language not okay for everyone, or longer than the time available.
2. **Per-person score (0–100).** Starting weights, to tune after testing: genre match 35, mood match 25, light/intense match 15, "something like" similarity 15, era match 10.
3. **Misery check.** Drop the title if any person scores under 40.
4. **Group score.** Average of everyone's scores, plus a small bonus for TMDB rating so strong titles win ties.
5. **Top 5.** Highest group scores go to the results page; "show more" loads the next 5.

```math
\text{GroupScore}(t) = \begin{cases} \frac{1}{n}\sum_{p=1}^{n} s_p(t) & \text{if } \min_p s_p(t) \ge 40 \\ \text{excluded} & \text{otherwise} \end{cases}
```

**If fewer than 5 titles survive:** lower the misery threshold step by step (40, then 30, then 20) and label those results "compromise picks", so the group knows why.

## TMDB filters used

Most answers map straight onto TMDB's Discover endpoints ([movies](https://developer.themoviedb.org/reference/discover-movie), [series](https://developer.themoviedb.org/reference/discover-tv)); in their filters a comma means AND and a pipe means OR.

| Answer | Movies (`/discover/movie`) | Series (`/discover/tv`) |
| --- | --- | --- |
| S1 Movie / Series | Use this endpoint | Use this endpoint |
| P2, Q2 Age | `certification_country=IN` + `certification.lte` | No age filter here: fetch each show's content ratings once, store in MySQL, filter there |
| P4, Q6 Languages | `with_original_language` | `with_original_language` |
| P6, Q3, Q4 Genres and mood | `with_genres`, pipe-separated (any of) | `with_genres`, pipe-separated |
| P7, Q5 Hard no | `without_genres` | `without_genres` |
| S3a Time (movie) | `with_runtime.lte` | — |
| S3b Time (series) | — | `with_runtime.lte`, `with_status` (finished vs ongoing) |
| P9 Era | `primary_release_date.gte` / `.lte` | `first_air_date.gte` / `.lte` |
| Q7 Light or intense | `with_keywords` / `without_keywords` (see [Keyword resolution](#keyword-resolution)) | `with_keywords` / `without_keywords` (see [Keyword resolution](#keyword-resolution)) |
| P8, Q8 Loved / "something like" | `/movie/{id}/recommendations` | `/tv/{id}/recommendations` |
| Quality floor (always on) | `vote_count.gte`, `vote_average.gte` | `vote_count.gte`, `vote_average.gte` |

**Two data traps:**

- **Runtime can be wrong in Discover.** TMDB checks runtimes from every translation, so a wrong value in one language can let a 167-minute movie pass a short-runtime filter ([TMDB forum](https://www.themoviedb.org/talk/5d6ee5296aa8e0735e424e6c)). Save each title's details in MySQL and re-check runtime there.
- **Series have no age filter in Discover.** Same fix: store content ratings per show and filter in SQL.

## Open questions

None right now. Decided on 27 Sep 2026: India only, age bands instead of exact age, a title's language must be okay for everyone, starting weights and misery threshold 40 kept until testing, US rating as fallback when no Indian certificate exists, and unrated titles shown with a "Not rated" badge.

## Sources

- [TMDB API: Discover Movie](https://developer.themoviedb.org/reference/discover-movie)
- [TMDB API: Discover TV](https://developer.themoviedb.org/reference/discover-tv)
- [TMDB forum: incorrect results for with_runtime.lte](https://www.themoviedb.org/talk/5d6ee5296aa8e0735e424e6c)
- [TMDB forum: Indian TV certifications](https://www.themoviedb.org/talk/63b7df66f44f2700a020d6f4)
- [Wikipedia: Central Board of Film Certification](https://en.wikipedia.org/wiki/Central_Board_of_Film_Certification)
- [TMDB TV Bible: Genre & Keywords](https://www.themoviedb.org/bible/tv/59f73eb49251416e71000024)
- [An introduction to group recommender systems (Towards Data Science)](https://towardsdatascience.com/an-introduction-to-group-recommender-systems-8f942a06db56/)
- [Masthoff: Group Recommender Systems, Combining Individual Models](https://www.researchgate.net/publication/227132202_Group_Recommender_Systems_Combining_Individual_Models)
