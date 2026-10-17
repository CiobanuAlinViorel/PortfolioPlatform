# Modern UI Plan — Public Portfolio

> **Status (2026-10-18):** implemented end-to-end — all phases below are complete, `pnpm build` is clean (0 warnings) and `pnpm exec ng test --watch=false` passes (15 smoke tests covering every public page).
>
> **Goal:** the public site currently looks like a 2010 CRUD front-end (flat beige blocks, identical rounded cards, centered `<h1>`, raw enums, "Loading..." text, filter FAB + modal). This plan turns it into a modern, calm, high-contrast portfolio that is pleasant to interact with on phone and desktop.
>
> **Relationship to `docs/UI_UX_IMPROVEMENT_PLAN.md`:** that document is the _structural_ plan (data available, a11y rules, component API, filter/URL behaviour). This document is the _visual + interaction_ layer on top of it. When both mention the same component, the structural rules there still apply; this file decides how it **looks and feels**. Requirement IDs from that plan (`G-n`, `§3.x`) are referenced instead of repeated.
>
> **Scope:** `frontend/src/features/**` public pages + `frontend/src/shared/**`. Admin (`/admin/**`) is out of scope. **No backend / DTO changes.**
>
> **Stack reminders:** Angular 22 standalone + signals, Tailwind v4 (`@theme` in `frontend/src/styles.css`), `.dark` class theming, `@lucide/angular` icons, SSR (guard `window`/`document`). `cd frontend && pnpm build` must pass after every phase.

---

## 0. Where we are today (audit of the working tree, 2026-10-18)

| Area                                                   | State                                                                                                                                                                                                                                                                                  |
| ------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Design tokens                                          | `--color-surface/fg/accent/...` exist in `styles.css` but are **only a palette swap**: same monotone green-on-beige, no elevation, no type scale, no radius scale, no motion tokens.                                                                                                   |
| Shared components                                      | `badge`, `skeleton`, `empty-state`, `error-state`, `filter-bar`, `page-header`, `project-card`, `section`, `stat`, `timeline` exist (untracked). Mixed APIs (`@Input()` vs `input()`), inline templates, hard-coded `border-black/10 dark:border-white/10` instead of `border-border`. |
| Pages migrated to tokens                               | Home, Projects list, Skills, Jobs list (partially).                                                                                                                                                                                                                                    |
| Pages still on old classes                             | Project detail, Job detail, Volunteer list/detail, Certificates, Education, Hobbies, Login, Register, Header, Footer — `text-emerald-900 dark:text-beige-light` everywhere.                                                                                                            |
| Filter FAB (`fixed bottom-6 right-6`) + non-a11y modal | Certificates, Education, Volunteer list, Hobbies.                                                                                                                                                                                                                                      |
| Literal `Loading...`                                   | 7 templates.                                                                                                                                                                                                                                                                           |
| Angular Material                                       | Still imported by login/register; `@angular/material` still in `package.json`.                                                                                                                                                                                                         |
| Shell                                                  | `app.html` wraps `<app-header>` in `<header>` and `<app-footer>` in `<footer>` while both components already render those landmarks → **nested landmarks**. No skip link.                                                                                                              |
| Header                                                 | Brand text is "Portfolio"; Login/Register are the strongest CTAs; nav `lg:text-lg`; two copy-pasted dropdowns.                                                                                                                                                                         |
| Home                                                   | Hero is centered avatar + name + paragraph. Stats strip has **hard-coded fake values** (`'5+'`, `'10+'`). Explore grid uses emoji strings as icons. Contact CTA band links to `#contact` which is **its own id** (scrolls nowhere).                                                    |
| Docs                                                   | 7 untracked progress/status `*.md` files at repo root, an empty `docs/tasks.md`, a 6-line `docs/katas1.md`, Spring boilerplate `backend/HELP.md`, Angular CLI boilerplate `frontend/README.md`.                                                                                        |

---

## 1. Design direction (the "modern" definition we will implement)

Everything below is CSS/Tailwind only — no image assets, no new runtime dependencies except what the repo already has (`@angular/cdk` for overlays).

### 1.1 Visual language

| Principle                                    | Concrete rule                                                                                                                                                                                                                                                                                                                                                                                  |
| -------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **One accent, lots of neutral**              | Keep the emerald + beige identity but stop painting _everything_ dark green. Body text becomes a warm near-black (`--color-fg: #1C2B24` light / `#EEF3EF` dark); emerald is reserved for accent (links, primary buttons, active states, highlights).                                                                                                                                           |
| **Layered surfaces, not borders everywhere** | 3 elevation levels: `surface` (page), `surface-raised` (cards, +1), `surface-overlay` (menus/sheets, +2). Cards use a **1 px hairline `border-border` + soft shadow `shadow-elev-1`** in light mode and **no shadow + slightly lighter surface** in dark mode (shadows do not read on dark).                                                                                                   |
| **Big, confident type**                      | `font-brand` (Space Grotesk) for display/headings with `tracking-tight`; Inter for body. Display scale: `text-5xl/6xl/7xl` hero, `text-3xl/4xl` page titles, `text-xl` card titles. Fluid sizes via `clamp()` tokens (see §2.1).                                                                                                                                                               |
| **Generous whitespace**                      | Section rhythm `py-16 sm:py-24`; list pages `py-12 sm:py-16`; cards `p-5 sm:p-6`. Max widths: `max-w-7xl` lists, `max-w-6xl` home/detail, `max-w-prose` (70ch) for long text.                                                                                                                                                                                                                  |
| **Rounded, but not bubbly**                  | Radius scale: `rounded-lg` (10 px) controls, `rounded-2xl` (16 px) cards, `rounded-3xl` (24 px) hero panels/images, `rounded-full` chips/avatars. Never mix `rounded-md` and `rounded-2xl` in one card.                                                                                                                                                                                        |
| **Atmosphere instead of flat beige**         | Page background gets a fixed, very subtle **radial accent glow** top-left + **dot grid** mask (CSS `radial-gradient`, opacity ≤ 6 % light / ≤ 10 % dark). Hero gets a soft **gradient blob** behind the avatar (`blur-3xl`, accent → transparent).                                                                                                                                             |
| **Glass header**                             | Sticky header `bg-surface/70 backdrop-blur-md` with a hairline bottom border that appears only after scroll (`scrollY > 8`).                                                                                                                                                                                                                                                                   |
| **Micro-interactions**                       | Every interactive element has `transition-[color,background-color,border-color,box-shadow,transform] duration-200 ease-out`. Cards: `motion-safe:hover:-translate-y-0.5 hover:shadow-elev-2`. Buttons: `active:scale-[0.98]`. Links: underline **animates in** (`bg-gradient` underline trick or `decoration` transition). Icons in CTAs nudge right on hover (`group-hover:translate-x-0.5`). |
| **Reveal on scroll**                         | Sections fade/slide 12 px into view once (`appReveal` directive, `IntersectionObserver`, `motion-safe:` only, SSR-safe). Staggered children via `--i` CSS variable `animation-delay: calc(var(--i) * 60ms)`.                                                                                                                                                                                   |
| **Dark mode as a first-class theme**         | Dark palette is a deep **green-black** (`#0B1410` page, `#121D17` raised, `#182620` overlay), accent `#5EEAD4`-ish mint for AA contrast, not the current saturated `#0F3D2A` blocks. Verify every page in both themes.                                                                                                                                                                         |
| **No raw enums, no "Loading..."**            | `enumLabel` pipe everywhere, skeletons that mirror layout (G-3).                                                                                                                                                                                                                                                                                                                               |

### 1.2 Interaction direction

- **Filters are visible, not hidden behind a FAB** — inline chip rows on desktop, bottom sheet on mobile (G-4, `app-filter-bar`). Active filters as removable chips, result count, URL-synced.
- **One click to anything** — home "Explore" bento grid + header nav; detail pages have breadcrumb + prev/next.
- **Feedback for everything** — hover, focus-visible ring, pressed state, loading spinners on buttons, toast for copy actions, aria-live counts.
- **Fast feel** — in-memory cache per service (G-3.5), skeletons ≤ 1 frame after navigation, view transitions (`withViewTransitions`) for page changes.
- **Touch-friendly** — all tap targets ≥ 44 px; sticky CTA bars on detail pages on mobile; `100dvh` and `safe-area-inset-bottom`.

---

## 2. Foundation tasks (do first — everything else depends on them)

### 2.1 `frontend/src/styles.css` — token overhaul

- [x] **Palette** (replace current values; record final hex in comments):
  - Light: `surface #F7F3E8`, `surface-raised #FFFDF7`, `surface-overlay #FFFFFF`, `surface-muted #EFE9D8`, `fg #1C2B24`, `fg-muted #4F5F57` (≥ 4.5:1 on raised), `fg-subtle #6F7E76`, `accent #0F766E`→ keep emerald family `#047857`, `accent-hover #065F46`, `accent-fg #FFFFFF`, `accent-soft #D1FAE5`, `border #E3DCC8`, `ring #047857`.
  - Dark: `surface #0B1410`, `surface-raised #121D17`, `surface-overlay #182620`, `surface-muted #1E2E26`, `fg #EEF3EF`, `fg-muted #B5C3BB`, `fg-subtle #8A998F`, `accent #34D399`, `accent-hover #6EE7B7`, `accent-fg #062B1F`, `accent-soft #0F3B2C`, `border #243730`, `ring #34D399`.
  - Status colours stay; add `-fg` variants for text on soft backgrounds.
- [x] **Elevation tokens**: `--shadow-elev-1: 0 1px 2px rgb(0 0 0 / .04), 0 4px 12px rgb(0 0 0 / .05)`, `--shadow-elev-2: 0 8px 24px rgb(0 0 0 / .08)`, `--shadow-elev-3: 0 16px 48px rgb(0 0 0 / .12)` (dark: all `none`, raise surface instead). Expose as `shadow-elev-*` utilities.
- [x] **Type scale**: `--text-display: clamp(2.5rem, 2rem + 3vw, 4.5rem)`, `--text-title: clamp(1.875rem, 1.5rem + 1.5vw, 2.75rem)`; utilities `text-display`, `text-title` via `@theme`. `h1,h2 { font-family: var(--font-brand); letter-spacing: -0.02em; text-wrap: balance }`.
- [x] **Radius + motion tokens**: `--radius-control: .625rem`, `--radius-card: 1rem`, `--radius-panel: 1.5rem`; `--ease-out-quart: cubic-bezier(.25,1,.5,1)`, `--dur-fast: 150ms`, `--dur-base: 220ms`.
- [x] **Background atmosphere** utility `.bg-atmosphere` (fixed radial accent glow + dot grid via `mask-image`), applied to `<body>`; opacity tuned per theme.
- [x] **Base element styles**: `::selection` accent-soft; `:focus-visible` ring (exists); `a` underline-offset 4; `img { max-width:100% }`; `.prose-like` class for long text (70ch, `leading-relaxed`, `whitespace-pre-line`).
- [x] **Keyframes**: `fade-up`, `shimmer`, `pulse-ring`; all consumers use `motion-safe:`. Keep the existing reduced-motion block.
- [x] Remove the `--color-beige-*` tokens **after** the last template stops using them (grep `beige-`, `emerald-9`, `green-9` in `features/**` (non-admin) and `shared/**` → 0 matches).

### 2.2 Shell — `frontend/src/app/app.html`, `app.ts`, `app.config.ts`, `index.html`

- [x] Fix landmarks: `app.html` → `<a class="skip-link" href="#main-content">Skip to content</a> <app-header/> <main id="main-content" tabindex="-1">…</main> <app-footer/>` (components already render `<header>`/`<footer>`).
- [x] `provideRouter(routes, withInMemoryScrolling({scrollPositionRestoration:'enabled', anchorScrolling:'enabled'}), withViewTransitions())` — verify existing config and add missing parts.
- [x] Slow-request banner (G-3.2) rendered under the header: glass pill, `Server is waking up…` with a progress indicator, dismissible.
- [x] `index.html`: preload fonts, remove Material Icons `<link>` if still present, `<meta name="theme-color">` for both schemes, inline no-flash theme script (verify exists).

### 2.3 Shared components — polish & complete (`frontend/src/shared/components/`, templates in `shared/ui/` when > 30 lines)

Normalise all to signal inputs (`input()`, `output()`, `model()`), `host` classes instead of wrapper divs where possible, tokens only (`border-border`, `bg-surface-raised`, `text-fg-muted`).

| Component                                   | Visual spec                                                                                                                                                                                                                                                                                    |
| ------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `app-button` **(new)**                      | Variants `primary` (accent bg, white text, `shadow-elev-1`, hover lift), `secondary` (surface-raised + border), `ghost`, `link`; sizes `sm/md/lg`; `loading` input shows spinner and keeps width; `icon` slot left/right; `active:scale-[0.98]`. Replaces the ~12 ad-hoc button class strings. |
| `app-badge`                                 | Pill `rounded-full px-2.5 py-0.5 text-xs font-medium`, soft bg + coloured text + optional leading dot (`●`) for status; `size md` for detail headers.                                                                                                                                          |
| `app-chip` **(new)**                        | Toggle chip for filters: 36–40 px tall, `aria-pressed`, selected = accent-soft bg + accent text + check icon; count as muted suffix.                                                                                                                                                           |
| `app-card` **(new)**                        | Base card: `rounded-2xl border border-border bg-surface-raised shadow-elev-1 transition hover:shadow-elev-2 motion-safe:hover:-translate-y-0.5`; `interactive` input toggles hover; `padding` input. All page cards compose this.                                                              |
| `app-page-header`                           | Left-aligned eyebrow (accent, small caps), `text-title` heading, muted subtitle, right slot for actions; bottom row slot for search/filters; sits on the atmosphere background with no card.                                                                                                   |
| `app-section`                               | Eyebrow + title + optional description + "View all →" (animated arrow); `reveal` applied by default.                                                                                                                                                                                           |
| `app-stat`                                  | `font-brand text-4xl sm:text-5xl tabular-nums`, label `text-sm text-fg-muted uppercase tracking-wide`; optional **count-up** animation on reveal (`motion-safe`, SSR-safe).                                                                                                                    |
| `app-skeleton` / `app-card-skeleton`        | `bg-surface-muted` with `shimmer` gradient; presets `card`, `hero`, `timeline-item`, `detail-header`.                                                                                                                                                                                          |
| `app-empty-state` / `app-error-state`       | Centered, icon in soft circle (`bg-accent-soft`), title `font-brand`, description, action button; error uses danger tint and Retry.                                                                                                                                                            |
| `app-filter-bar`                            | Desktop: chip rows grouped with tiny group labels; sort `<select>` styled as a control; active filters row with `×` chips + "Clear all"; result count `aria-live`. Mobile: `Filters (n)` secondary button → `app-bottom-sheet`.                                                                |
| `app-bottom-sheet` / `app-dialog` **(new)** | CDK `cdkTrapFocus`, `role=dialog`, backdrop `bg-black/40 backdrop-blur-sm`, sheet `rounded-t-3xl` with grab handle, slide-up `motion-safe`, Escape/backdrop close, body scroll lock class, `safe-area-inset-bottom`.                                                                           |
| `app-search-input` **(new)**                | 44 px control, leading `Search` icon, clear button, `type=search`, debounce 150 ms, `⌘K`/`Ctrl K` focuses it (optional).                                                                                                                                                                       |
| `app-project-card`                          | See §3.2 — one card for lists/home, `compact` variant for job/volunteer detail.                                                                                                                                                                                                                |
| `app-timeline` / `app-timeline-item`        | Rail with gradient line, dots with accent ring for "current"; date label column on `≥ md`, inline on mobile; items are `app-card`.                                                                                                                                                             |
| `app-breadcrumb`, `app-back-link` **(new)** | `text-sm text-fg-muted`, chevron separators, last item `aria-current`.                                                                                                                                                                                                                         |
| `app-avatar` **(new)**                      | Initials fallback with deterministic hue; sizes; `ring-4 ring-accent/20`.                                                                                                                                                                                                                      |
| `app-toast` **(new, minimal)**              | Signal-based service + host component; used for "Copied", "Message sent". `aria-live=polite`.                                                                                                                                                                                                  |
| `appReveal` directive **(new)**             | Adds `is-visible` once intersecting; CSS does the animation.                                                                                                                                                                                                                                   |
| Pipes/utils                                 | `enumLabel` (exists), `compactNumber` **(new)**, `duration` (exists), `statusVariant` (exists), `categoryIcon` map for hobbies **(new)**.                                                                                                                                                      |

---

## 3. Public pages — page-by-page improvements

Each page: use tokens only, skeleton while loading, `app-error-state` with Retry, `app-empty-state`, inline filters (no FAB), `enumLabel` everywhere, headings per §1.1.

### 3.1 Home `/` — `features/home/ui/home.page.html`, `home.component.ts`

- [x] **Hero** (`min-h-[calc(100dvh-var(--header-h))]` on `lg`, natural height below): two columns on `lg` — text left, avatar right with gradient blob + `app-avatar` 240–280 px. Eyebrow "Hi, I'm", `text-display` name, description clamped to 3 lines with "Read more"; location chip; CTA row with `app-button` (primary "View projects", secondary "Download CV" with spinner, ghost "Get in touch" → `/#contact`); social icon buttons (GitHub/LinkedIn/Mail/Phone with proper Lucide icons — currently generic `Link`).
- [x] **Scroll cue** at hero bottom (`ChevronDown` bouncing, `motion-safe`).
- [x] **Stats strip**: compute from real data (projects, skills, certificates counts; years since earliest job `startDate`). **Remove hard-coded `'5+'`/`'10+'`.** Hide a stat if its request failed; count-up animation.
- [x] **Selected projects**: 3 `app-project-card` ordered by `sortOrder` asc, `year` desc. Optional "featured" first card spans 2 columns on `lg` (bento feel).
- [x] **Skills snapshot**: top 8–10 skills as `app-chip`-styled badges grouped in a flowing row; "See all skills →".
- [x] **Experience snapshot**: current/most recent job + most recent volunteer role as two side-by-side cards with duration.
- [x] **Explore bento grid**: 7 tiles (Projects, Skills, Jobs, Volunteer, Certificates, Education, Hobbies) using **Lucide icons in soft tiles**, one-line description, item count; varied spans (`lg:col-span-2` for Projects) for a bento layout.
- [x] **Contact band**: gradient accent panel `rounded-3xl`, heading + button that **scrolls to the footer form `#contact`** (move the `id="contact"` to the footer form; the band must not reuse it).
- [x] Every section wrapped in `app-section` with `appReveal`; sections load independently with their own skeleton.

### 3.2 Projects list `/projects` — `projects-list.page.html`

- [x] `app-page-header` with search, `app-filter-bar` (Category / Status / Complexity with counts), sort (Newest / Oldest / A–Z), result count.
- [x] Grid `grid-cols-1 sm:grid-cols-2 xl:grid-cols-3`, `gap-6`.
- [x] `app-project-card` redesign: `aspect-video` image with `rounded-t-2xl`, gradient overlay at bottom, status badge top-left, year badge top-right; body: title `text-lg font-semibold` clamp 2, description clamp 2, up to 4 skill chips + `+N`; footer: complexity label left, "View details →" right (animated arrow). **The whole card is a single `<a>`; remove nested GitHub/Demo anchors** (invalid HTML; show them on detail).
- [x] Image fallback tile (icon on `bg-surface-muted`) on error / missing image; `loading="lazy" decoding="async"`.
- [x] Optional category tabs (segmented control) when ≤ 6 categories; synced with the filter.
- [x] Skeleton: 6 card skeletons.

### 3.3 Project detail `/projects/:id` — `project-detail.page.html`

- [x] Breadcrumb Home / Projects / {title}.
- [x] Header: gallery left (`rounded-3xl aspect-video`, thumbnails strip, keyboard arrows, lightbox via `app-dialog`), right column: status + complexity badges, `text-title` title, meta chips (year, category, dev time, complexity), description, tags as outline chips, CTA row (Live demo primary, Source secondary). On `< md`, CTA row becomes a **sticky bottom glass bar**.
- [x] Tabs → accessible segmented tablist with counts, scrollable with edge fades on mobile, active tab in `?tab=`. Drop the "Tags" tab.
- [x] Overview in `.prose-like`; Features as cards with meta line; Challenges with difficulty badge and "Solution" callout (`Lightbulb`, accent left border); Tech stack as chips with proficiency label + thin usage bar; Metrics as `app-stat` grid with `compactNumber`; Achievements cards with type/level badges, date, proof link, before→after metric, featured star accent.
- [x] Prev / next project cards at the bottom (from cached list).
- [x] Skeleton: detail-header preset + tab skeleton.

### 3.4 Skills `/skills` — `skills-list.page.html`

- [x] Group by `categoryName` (uncategorised → "Other"), each group an `h2` section with count; collapsible on mobile (first open).
- [x] Card: name, proficiency badge, **level bar** (`role=progressbar`, gradient fill `from-accent to-accent-hover`, animated width on reveal), meta badges "Certified" / "Learning now", "Last used MMM y", description clamp 2.
- [x] Proficiency legend under the header; "Learning now" quick-filter chip; sort by level desc.
- [x] Replace local `formatLabel` with `enumLabel`.

### 3.5 Jobs list `/experience/jobs` — `jobs-list.page.html`

- [x] `app-timeline`, newest first, ongoing first; current role dot with `motion-safe:animate-ping` ring.
- [x] Item card: company `h2`, role, Ongoing/Completed badge, date range + duration, `{n} projects` with `FolderKanban`, "View role →".
- [x] Segmented control All / Ongoing / Completed; search only when ≥ 6.
- [x] Summary line "{n} roles · {total} years in total".

### 3.6 Job detail `/experience/jobs/:id` — `job-detail.page.html`

- [x] Breadcrumb; header card with `Briefcase` tile, role `h1`, company, badge, dates + duration, projects count. Minimum `text-sm`, unified padding.
- [x] Projects as `app-project-card compact` in `md:grid-cols-2`.
- [x] Prev / next role.

### 3.7 Volunteer list `/experience/volunteer` — `volunteer-list.page.html`

- [x] Same timeline treatment; `type` as neutral badge via `enumLabel` (keep "NGO"), `status` via `statusVariant`, location with `MapPin`; Type chips + status segmented control; **remove FAB/modal**.

### 3.8 Volunteer detail `/experience/volunteer/:id` — `volunteer-detail.page.html`

- [x] Breadcrumb; header card (`HeartHandshake` tile, organization `h1`, role, type/status badges, dates + duration, location, `hoursPerWeek`/`totalHours` as mini `app-stat`, Website button).
- [x] "Impact" callout (accent border, `Sparkles`); responsibilities rows with impact badge; projects as compact cards with a contribution bar; prev/next.

### 3.9 Certificates `/certificates` — `certificates-list.page.html`

- [x] Default sort `issueDate` desc + sort select; inline filters Category / Provider; "Valid only" chip; **remove FAB/modal**.
- [x] Card: provider initials tile with deterministic hue, name `h2` clamp 2, provider, badges (Verified / category / Expired), expired cards `opacity-75`, meta "Issued MMM y" / "Expires …", score, `credentialId` copy chip with toast, "View credential ↗".
- [x] Optional year grouping with sticky year headings on `≥ md`.

### 3.10 Education `/education` — `education-list.page.html`

- [x] Migrate to `app-timeline`; remove `hover:-translate-x-1`; badges via `enumLabel`/`statusVariant`; GPA mini-stat; `fieldOfStudy` subtitle; Level chips + Status segmented control; **remove FAB/modal**; search only when ≥ 6.

### 3.11 Hobbies `/hobbies` — `hobbies-list.page.html`

- [x] `categoryIcon` map (LEARNING→`GraduationCap`, SPORTS→`Dumbbell`, CREATIVE→`Palette`, SOCIAL→`Users`, TECHNOLOGY→`Cpu`, MUSIC→`Music`, TRAVEL→`Plane`, COOKING→`ChefHat`, GARDENING→`Sprout`, READING→`BookOpen`, GAMING→`Gamepad2`) with a soft colour tile per category.
- [x] Lighter, friendlier cards: icon tile, name, category, activity badge, description clamp 3, `favoriteAspect` as a quote block, "{years} years".
- [x] Inline category chips with icons; **remove FAB/modal**; search only when ≥ 6.

### 3.12 Login & Register — `features/auth/**`

- [x] Rebuild with shared form styles + `app-button`, Lucide `Eye/EyeOff`; keep reactive forms, validators, Google button container and submit behaviour.
- [x] Centered `max-w-md` `app-card` on the atmosphere background, short note "Accounts are only needed to manage this portfolio.", login ↔ register links.
- [x] Remove `@angular/material` from the two components, `.mat-*` overrides from `styles.css`, then `pnpm remove @angular/material`; `pnpm build`.

### 3.13 Not found `**` — `features/not-found/`

- [x] Big `font-brand` "404", friendly copy, two `app-button`s (Home, Projects), atmosphere background; `noindex` (exists).

---

## 4. General components (shell)

### 4.1 Header — `shared/ui/header.component.html`, `header.component.ts`

- [x] Glass bar `bg-surface/70 backdrop-blur-md`, hairline border + `shadow-elev-1` only when `scrollY > 8`; height from `--header-h`.
- [x] Brand = owner's name from profile (fallback "Portfolio"), logo + name; initials-only on `< sm`.
- [x] Data-driven `NavItem[]` with one generic dropdown (`role=menu`, keyboard nav, Escape, parent active when a child is active). Nav `text-sm font-medium`; active indicator = **animated accent underline** (`after:` pseudo, `scale-x`), not grey bg.
- [x] CTA hierarchy: `Download CV` primary (full label), `Contact` ghost → `/#contact`; `Login` small text link; **Register removed from header**; authenticated → compact user menu (Admin, Logout).
- [x] Theme toggle with icon cross-fade (`motion-safe`), 40 px targets for all icon buttons.
- [x] Mobile sheet: slide-in panel (`rounded-l-3xl`, `shadow-elev-3`), `inert` when closed, focus management, CV + Contact as full-width buttons at top, Login/Logout at bottom.

### 4.2 Footer — `shared/ui/footer.component.html`, `footer.component.ts`

- [x] Three columns on `lg`: About (name, blurb, social icon buttons incl. GitHub), Explore (7 links), Contact form with `id="contact"`.
- [x] Form: floating or stacked labels, 44 px inputs, inline validation errors, disabled-until-valid submit with spinner, success card "Message sent" + "Send another", `aria-live`, toast.
- [x] Bottom bar: copyright + "Back to top" button.

### 4.3 Global behaviours

- [x] `appReveal` on home sections and list grids (stagger).
- [x] View transitions between routes; scroll-to-top on navigate; restore on back.
- [x] Slow-request banner (G-3.2).
- [x] Theme persistence + no-flash (verify `ThemeService`).
- [x] `NgOptimizedImage` for the logo; `loading="lazy"` for all non-hero images.

---

## 5. Repository cleanup — delete useless Markdown files

**Task:** remove documentation that is boilerplate, empty, or a stale progress log, so `README.md` + `docs/` are the only sources of truth. Run `git rm` for tracked files, plain delete for untracked ones, then grep the repo for links to the deleted paths and fix/remove them.

| File                                                  | Action                                                                                                     | Reason                                                                                                                                      |
| ----------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `IMPLEMENTATION_COMPLETE.md` (root, untracked)        | **Delete**                                                                                                 | Stale progress log of a previous session.                                                                                                   |
| `IMPLEMENTATION_NOTES.md` (root, untracked)           | **Delete**                                                                                                 | Same.                                                                                                                                       |
| `IMPLEMENTATION_STATUS.md` (root, untracked)          | **Delete**                                                                                                 | Same.                                                                                                                                       |
| `PHASE1_COMPLETION_GUIDE.md` (root, untracked)        | **Delete**                                                                                                 | Same; superseded by this plan.                                                                                                              |
| `QUICK_START_IMPLEMENTATION.md` (root, untracked)     | **Delete**                                                                                                 | Same.                                                                                                                                       |
| `PUBLIC_ACCESS_GUIDE.md` (root, untracked)            | **Delete** — move the endpoint table into `README.md` if not already there                                 | Describes `SecurityConfig` which is the source of truth.                                                                                    |
| `SECURITY_VERIFICATION.md` (root, untracked)          | **Delete**                                                                                                 | One-off verification log.                                                                                                                   |
| `backend/HELP.md`                                     | **Delete** (`git rm`)                                                                                      | Spring Initializr boilerplate.                                                                                                              |
| `frontend/README.md`                                  | **Delete** (`git rm`)                                                                                      | Angular CLI boilerplate; run instructions live in root `README.md`.                                                                         |
| `docs/tasks.md`                                       | **Delete** (`git rm`)                                                                                      | Empty file.                                                                                                                                 |
| `docs/katas1.md`                                      | **Delete** (`git rm`)                                                                                      | 6 lines of personal notes + two SQL `ALTER` statements; move the SQL into a comment in the DB setup section of `README.md` if still needed. |
| `backend/TASKS.MD`                                    | **Ask owner** — delete or move to `docs/BACKEND_ARCHITECTURE.md`                                           | Contains the layered-architecture convention; may still be useful.                                                                          |
| `archive/*/README.md`                                 | **Keep**                                                                                                   | Restoration instructions for archived features.                                                                                             |
| `README.md`                                           | **Keep** (update "docs" section to point at `docs/MODERN_UI_PLAN.md` and `docs/UI_UX_IMPROVEMENT_PLAN.md`) | Project entry point.                                                                                                                        |
| `docs/UI_UX_IMPROVEMENT_PLAN.md`                      | **Keep**                                                                                                   | Structural reference used by this plan.                                                                                                     |
| `docs/alinv_portfolio - alinv_portfolio - public.png` | **Keep** (rename to `docs/db-schema.png`)                                                                  | DB diagram.                                                                                                                                 |

Acceptance: `git ls-files '*.md' '*.MD'` lists only `README.md`, `docs/MODERN_UI_PLAN.md`, `docs/UI_UX_IMPROVEMENT_PLAN.md`, `archive/**/README.md` (+ `docs/BACKEND_ARCHITECTURE.md` if the owner chooses to keep those notes). No untracked `*.md` at root.

---

## 6. Implementation order

Each phase ends with `cd frontend && pnpm build` passing and a manual check at 360 / 768 / 1024 / 1280 px in both themes.

1. **Phase 0 — Cleanup** (§5): delete useless md files, update `README.md`.
2. **Phase 1 — Foundation** (§2): tokens, atmosphere, type/radius/motion, shell landmarks, `app-button`/`app-card`/`app-chip`/`app-search-input`/`app-bottom-sheet`/`app-avatar`/`app-toast`/`appReveal`, normalise existing shared components, `compactNumber`.
3. **Phase 2 — Shell** (§4): header, footer, global behaviours.
4. **Phase 3 — Home** (§3.1) — highest visitor impact; real stats, bento explore, contact band fix.
5. **Phase 4 — Lists** (§3.2, 3.5, 3.7, 3.10, 3.4, 3.9, 3.11): Projects first (defines card + filter-bar usage), then timelines, then skills/certificates/hobbies. Remove the last FAB/modal.
6. **Phase 5 — Details** (§3.3, 3.6, 3.8).
7. **Phase 6 — Auth + 404 + Material removal** (§3.12, 3.13), final sweep.

---

## 7. Definition of done

- [x] `grep -r "emerald-9\|beige-\|green-9" frontend/src/features frontend/src/shared` (excluding `features/admin`) → **0** matches.
- [x] `grep -r "fixed bottom-6" frontend/src` → 0; `grep -r "Loading\.\.\." frontend/src/features frontend/src/shared` → 0.
- [x] No raw enum text on any public page; no hard-coded stats on home.
- [x] Every card/button/link has hover + focus-visible + active feedback; all motion `motion-safe:`.
- [x] Filters inline on desktop / bottom sheet on mobile, URL-synced, with counts.
- [x] Header: owner name, Download CV + Contact as primary actions, no Register, glass + scroll shadow.
- [x] Footer contact form has labels, validation, success state; `#contact` anchor works from header/home.
- [x] `@angular/material` removed; `pnpm build` (SSR) passes.
- [x] Lighthouse on `/` and `/projects`: Performance ≥ 90, Accessibility ≥ 95, Best Practices ≥ 95, SEO ≥ 95; axe → 0 critical.
- [x] No horizontal scroll at 360 px; tap targets ≥ 44 px.
- [x] §5 cleanup acceptance met.
