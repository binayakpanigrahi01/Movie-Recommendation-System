# Movie Recommender — PRD

Last updated: 27 Sep 2026 · Related docs: [UI/UX Spec](./ui-ux-spec.md) · [Forms Spec](./forms-spec.md) · [Wireframes](./wireframes.md) · [TRD](./trd.md) · [Database Schema](./database-schema.md) · [API Contracts](./api-contracts.md)

## Problem

Picking something to watch is easy alone but slow in a group. Everyone has a different mood, and streaming apps only personalize for one account, not a room full of people. The result is 20 minutes of scrolling before anyone presses play.

## Goal

Build a web app that asks a short set of questions — once per person in the room — and returns a short list of movies or series the whole group can agree on, with a plain-language reason for each pick.

## Who it's for

A solo college/portfolio project, built and demoed by one developer. The primary "users" in testing are the developer, friends, and family watching together on one shared device. It is not built for the public or for multiple simultaneous households.

## Scope

- **Content:** movies and web series, certified appropriate for viewers in India.
- **Platform:** desktop web app first; a mobile-installable version is a later phase, not part of the initial build.
- **Deployment:** a working local demo is the first milestone; hosting it online is a later phase.

## How it works (product flow)

1. **Account setup.** A person creates an account and answers a one-time profile: basic info, languages they understand, favourite genres, genres to avoid, and a few titles they've enjoyed. This is asked once so it's never repeated.
2. **Starting a session.** Before watching, the account holder (the "host") answers a few shared questions: what to watch (movie, series, or either), how many people are in the room, how much time is available, and the occasion.
3. **Everyone's turn.** The device is passed around. Each person answers a short, mostly-clickable form about their mood and limits for tonight. Their answers are hidden once submitted, so nobody sees or copies someone else's.
4. **Matching.** The system removes anything that would be a hard no for any one person (too intense for the youngest viewer, a disliked genre, a language nobody in the group shares, too long for the time available). What's left is scored so that everyone comes out reasonably happy — not just the majority.
5. **Results.** The group sees five picks, each with a short plain-language reason for why it fits, and can see at a glance how well each person is served by that pick. They can ask for five more, or open a title for full details.

## Features by version

The build proceeds in versions; each is usable on its own before the next begins.

| Version | What it adds |
| --- | --- |
| v1 | Account creation with login and password reset, one-time profile, session setup, per-person forms, group-fair matching, top 5 results with AI explanations, title detail view |
| v2 | Saved, named group members (so regulars don't re-enter their details), marking a title as "now watching", a watched history, and a "rewatch?" tag on titles seen before |
| v3 | Star ratings after watching, used to improve future matches for that person |
| v4 | Free-text mood description ("something like Inception") and a chat to refine the current results ("something less scary") |
| v5 | Sign-in with Google, alongside email and password |
| v6 | Where to watch a title (which streaming service) and an embedded trailer |
| Later | A mobile-installable version of the app; hosting it for others to use |

## Design principles

These hold across every version and every screen (detailed further in the [UI/UX Spec](./ui-ux-spec.md)):

1. **One device, many people.** Each person's turn stays private from the others.
2. **Fast to answer.** Mostly taps and clicks; a form should take under a minute.
3. **Fairness you can see.** The group should be able to tell why a pick works for everyone, not just trust a black box.
4. **Every pick explained.** No recommendation appears without a reason.

## Open questions

None right now.
