# Antigravity System Rules & Guidelines

These rules are mandatory for Antigravity when working on the **Movie Recommendation System** project.

---

## 1. Git Command Restrictions (STRICT)

Antigravity must adhere to the following Git command restrictions:

### ❌ Strictly Prohibited Git Commands
Antigravity is **NEVER** allowed to execute any Git commands that write, modify, stage, commit, or alter the Git repository state. Prohibited commands include, but are not limited to:
- `git add`
- `git commit`
- `git push` / `git pull`
- `git checkout` / `git switch`
- `git reset` / `git revert`
- `git merge` / `git rebase`
- `git stash`
- `git rm` / `git clean`
- `git branch` (creation / deletion)

### ✅ Allowed Git Commands (Read-Only)
Antigravity is **ONLY** permitted to run the following non-mutating inspection commands:
- `git status` (to inspect untracked, modified, or staged files)
- `git log` (to check past commit history, capped with `-n <N>`)

---

## 2. Hand-off Protocol for Git Commits

- Antigravity must never automatically stage or commit files.
- When an implementation phase, feature, or milestone is completed and verified, Antigravity will present the exact `git add` and `git commit` commands at the very end of its response so the **USER** can review and execute them directly in their terminal.

---

## 3. General Development Guidelines

1. **Security & Secrets:**
   - Never commit sensitive information (passwords, JWT secrets, TMDB/Groq API keys).
   - All credentials must stay in `application-local.properties` or environment variables, which must remain strictly gitignored.
2. **Architecture & Project Structure:**
   - Maintain the monorepo structure: `/backend` (Spring Boot 3.x + Maven) and `/frontend` (React + Vite + TypeScript).
   - Respect API contracts defined in `docs/api-contracts.md` and database schemas in `docs/database-schema.md`.
3. **Design System & Aesthetics:**
   - Adhere to the **Midnight Cinema** theme (`#0E0F13` background, `#1A1C23` surface, `#E5A00D` gold accent, Bebas Neue headings, Inter body text).
   - Ensure high visual polish and keyboard accessibility (Tab, Enter, Space, Arrow keys).
4. **Verification First:**
   - Always verify changes via compiler and test commands (`.\mvnw.cmd compile`, `npm run build`, etc.) before reporting completion.
