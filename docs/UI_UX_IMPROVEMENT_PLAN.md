# UI/UX Improvement Plan — Public Portfolio Site

> **Audience:** any developer or LLM implementing this plan without prior context.
> **Scope:** the public (visitor-facing) Angular frontend only. The admin panel (`/admin/**`) is out of scope.
> **Constraint:** frontend-only. Do **not** change the backend, DTOs or API contracts. Every requirement below works with the data the API already returns.

---

## 0. Project context (read first)

| Item | Value |
|---|---|
| Frontend root | `frontend/` |
| Framework | Angular 22, standalone components, **signals** (`signal`, `computed`), new control flow (`@if`, `@for`, `@switch`), SSR + hydration enabled |
| Styling | Tailwind CSS v4 (`@import 'tailwindcss'` in `frontend/src/styles.css`, tokens in `@theme`), dark mode via `.dark` class on `<html>` (`@custom-variant dark`) |
| Icons | `@lucide/angular` — used as `<svg lucideXxx class="h-4 w-4"></svg>` or `<svg [lucideIcon]="icon">` with `LucideDynamicIcon`. Import each icon component into the component `imports` array |
| Fonts | `Space Grotesk` (`font-brand`) and `Inter` (`font-sans`) loaded from Google Fonts in `frontend/src/index.html` |
| Current palette | `--color-beige-light: #FBF5DD`, `--color-beige-dark: #E7E1B1`, Tailwind `emerald-*` / `green-900/950` |
| File convention | Component class in `features/<feature>/components/*.page.ts`, template in `features/<feature>/ui/*.page.html` (`templateUrl: "../ui/x.page.html"`), API in `features/<feature>/services/*.service.ts`, types imported from `features/admin/features/<feature>/types` |
| Shared code | `frontend/src/shared/components/` (header, footer) + `frontend/src/shared/ui/` (their templates) + `frontend/src/shared/services/` (theme, seo, contact, cv-download) |
| Routing | `frontend/src/app/app.routes.ts`, provider config in `frontend/src/app/app.config.ts` |
| Shell | `frontend/src/app/app.html` — public routes render `<app-header /> <router-outlet /> <app-footer />` |
| Run | `cd frontend && pnpm install && pnpm start` (API on `environment.api.baseUrl`; backend on Render free tier — **cold start ≈ 15 s**) |
| Build check | `cd frontend && pnpm build` must pass (SSR build — avoid touching `window`/`document` outside `isPlatformBrowser` guards or event handlers) |

### Public pages audited

| Route | Component | Template |
|---|---|---|
| `/` | `features/home/components/home.component.ts` | `features/home/ui/home.page.html` |
| `/projects` | `features/projects/components/projects-list.page.ts` | `features/projects/ui/projects-list.page.html` |
| `/projects/:id` | `features/projects/components/project-detail.page.ts` | `features/projects/ui/project-detail.page.html` |
| `/skills` | `features/skills/components/skills-list.page.ts` | `features/skills/ui/skills-list.page.html` |
| `/experience/jobs` | `features/experience/jobs/components/jobs-list.page.ts` | `.../ui/jobs-list.page.html` |
| `/experience/jobs/:id` | `features/experience/jobs/components/job-detail.page.ts` | `.../ui/job-detail.page.html` |
| `/experience/volunteer` | `features/experience/volunteer/components/volunteer-list.page.ts` | `.../ui/volunteer-list.page.html` |
| `/experience/volunteer/:id` | `features/experience/volunteer/components/volunteer-detail.page.ts` | `.../ui/volunteer-detail.page.html` |
| `/certificates` | `features/certificates/components/certificates-list.page.ts` | `.../ui/certificates-list.page.html` |
| `/education` | `features/education/components/education-list.page.ts` | `.../ui/education-list.page.html` |
| `/hobbies` | `features/hobbies/components/hobbies-list.page.ts` | `.../ui/hobbies-list.page.html` |
| `/login`, `/register` | `features/auth/{login,register}/components/*.page.ts` | `features/auth/{login,register}/ui/*.page.html` |
| shell | `shared/components/header.component.ts`, `shared/components/footer.component.ts` | `shared/ui/header.component.html`, `shared/ui/footer.component.html` |

### Data available per entity (what the UI may use — nothing else exists)

- **Profile** (`/profile/summary`): `firstName, lastName, age?, imageLink?, description?` + `contact: { email, phone?, github?, linkedin?, city?, country? }`. There is **no** headline/role/tagline field.
- **ProjectListItem**: `id, title, description?, status (PLANNING|DEVELOPMENT|TESTING|PRODUCTION|MAINTENANCE|ARCHIVED), complexity (BEGINNER|INTERMEDIATE|ADVANCED), year?, tags[], githubUrl?, demoUrl?, completionDate?, categoryName?, sortOrder?, developmentTime?, imageUrl?, skills: string[]`.
- **ProjectDetail**: list fields + `longDescription?, features[] {title, description?, implementationDate?, developmentTimeHours?}, challenges[] {title, description, solution?, difficulty LOW|MEDIUM|HIGH}, media[] {imageUrl, altText?, title?, description?, primary, sortOrder}, metrics? {usersCount, performanceScore, codeQualityScore, linesOfCode, commitsCount, testCoveragePercentage}, skills[] {id, name, proficiency, usage?}, achievements[] {title, description, achievementType, achievementDate, recognitionLevel, recognizedBy?, proofUrl?, isFeatured?, impactSummary?, demoUrl?, metricBefore?, metricAfter?}`.
- **SkillListItem**: `id, name, categoryName?, proficiency (BEGINNER|INTERMEDIATE|ADVANCED|EXPERT), level? (0-100), yearsOfExperience?, description?, lastUsedDate?, hasCertification?, learning?`.
- **JobExperienceListItem**: `id, companyName, role, startDate, endDate?, projectsCount`. **Detail** adds `projects[] {id, title, description?, status, year?, tags[], githubUrl?, demoUrl?}`. No description/responsibilities exist for jobs.
- **VolunteerExperienceListItem**: `id, organization, role, type (ASSOCIATION|CLUB|COMMUNITY|NGO|CHARITY), location?, startDate, endDate?, status (COMPLETED|ONGOING), projectsCount`. **Detail** adds `description?, website?, hoursPerWeek?, totalHours?, impactDescription?, responsibilities[] {description, impactLevel LOW|MEDIUM|HIGH}, projects[] {contributionPercentage, project{...}}`.
- **Certificate**: `id, name, provider, credentialId?, certificateUrl?, issueDate, expiryDate?, hasExpiry?, description?, score?, relevanceScore?, verified?, categoryName?`.
- **Education**: `degree?, fieldOfStudy?, institution, location?, level, status (incl. DROPPED), startDate, endDate?, description?, gpa?`.
- **Hobby**: `name, category (LEARNING|SPORTS|CREATIVE|SOCIAL|TECHNOLOGY|MUSIC|TRAVEL|COOKING|GARDENING|READING|GAMING), activityLevel?, complexityLevel?, description?, favoriteAspect?, yearsActive?`.

---

## 1. Executive summary of the audit

The site is functional but reads as a generic CRUD front-end rather than a portfolio designed to impress a visitor/recruiter:

1. **The home page is a business card, not a landing page** — avatar, name, age, one paragraph, contact links. No call-to-action, no preview of projects/skills/experience, no reason to scroll or click.
2. **Every page looks identical** — same beige card, same `rounded-xl shadow-sm`, same centered `<h1>`, same muted grey text. There is no visual hierarchy, rhythm, or brand personality.
3. **Raw database enums leak into the UI** (`PRODUCTION`, `INTERMEDIATE`, `NGO`, `COMPLETED`, `LOW`) on Projects, Volunteer and Jobs pages. Skills/Education/Hobbies format them with three different local helpers.
4. **Loading/error states are bare text** (`Loading...`, red sentence) — and the backend cold-starts for ~15 s, so the first impression is a blank beige page with the word "Loading...".
5. **~100 lines of search + floating-button + filter-modal markup are copy-pasted into 7 list pages**, each with a slightly different filter set. The modal is not accessible (no focus trap, no Escape, no `role="dialog"`), and the floating button hides filters on desktop and overlaps the footer on mobile.
6. **Accessibility gaps:** icon-only buttons without labels, no visible focus styles, hidden mobile menu is still focusable, tabs/gallery/progress bars lack ARIA, animations ignore `prefers-reduced-motion`, several text colors at 50–60 % opacity fail WCAG AA contrast on the beige background.
7. **Navigation/polish gaps:** no 404 page, no scroll-to-top on route change, theme preference not persisted (flashes/resets on reload), `console.log` in production shell, "Login/Register" are the most prominent header CTAs on a site whose visitors never need an account.
8. **Auth pages use Angular Material** (indigo theme + Material Icons font) — visually inconsistent with the Tailwind design and ~300 KB of extra CSS/JS/font for two forms.

---

## 2. Cross-cutting findings & requirements (apply to all public pages)

Each finding has an ID (`G-n`). Page-specific sections reference these IDs instead of repeating them.

### G-1 Design tokens & visual identity

**Finding.** Colors are hard-coded per element (`text-emerald-900 dark:text-beige-light` appears hundreds of times). Muted text uses arbitrary opacities (`/50`, `/60`, `/70`, `/80`, `/90`). The only typography differentiation is `font-bold`; the brand font is used solely on the logo text. Backgrounds: page = `beige-dark`/`green-950`, card = `beige-light`/`green-900`, header = `beige-light/90`. The look is flat and monotone.

**Requirements.**
1. Extend `@theme` in `frontend/src/styles.css` with semantic tokens and expose them as Tailwind utilities (Tailwind v4 reads `--color-*` automatically). Minimum set, each with a light and dark value (set dark values under `.dark { ... }` by overriding the CSS variables):
   - `--color-surface` (page bg), `--color-surface-raised` (cards), `--color-surface-overlay` (modals/sheets), `--color-surface-muted` (chips/inset blocks)
   - `--color-fg` (primary text), `--color-fg-muted` (secondary text — **must meet 4.5:1** on `surface-raised`), `--color-fg-subtle` (captions ≥ 3:1, only for ≥ 18 px/bold text)
   - `--color-accent`, `--color-accent-hover`, `--color-accent-fg` (text on accent), `--color-accent-soft` (tinted bg for badges)
   - `--color-border`, `--color-ring`
   - semantic status: `--color-success`, `--color-info`, `--color-warning`, `--color-danger` (+ `-soft` tints) for badges
2. Keep the existing beige/emerald character (the owner's brand) but **raise contrast**: muted text must be a solid color token, not an opacity of the heading color. Verify with a contrast checker; record the final hex values in `styles.css` comments.
3. Typography scale: use `font-brand` (Space Grotesk) for all `h1`/`h2` and hero numbers; `font-sans` (Inter) for body. Body text on cards: minimum `text-sm` (14 px) for metadata, `text-base` (16 px) for descriptions. Never use `text-xs` for paragraph content (currently done across job/volunteer detail).
4. Spacing rhythm: page sections use `py-16 sm:py-20` for the hero and `py-12 sm:py-16` for list pages; card padding `p-5 sm:p-6`; grid gaps `gap-5 sm:gap-6`. Apply consistently (today `py-6`, `py-10`, `py-12` are mixed).
5. Add a subtle background treatment so pages are not a flat beige block: e.g., a soft radial gradient / noise or a faint dot grid behind the hero and page headers (CSS only, no images). Must remain subtle in dark mode.
6. Define a shared `--header-h` CSS variable (see G-8) and replace every `min-h-[calc(100vh-3.5rem)]` with `min-h-[calc(100dvh-var(--header-h))]`.
7. Migrate existing templates to the new tokens incrementally (page by page as they are reworked); do not leave a mix of old/new within a single page.

### G-2 Shared UI component library (de-duplication)

**Finding.** Search bar, floating filter button, filter modal, chip buttons, empty state, loading text, error text, back-link, page header, status badge and "project mini card" are duplicated across pages with copy-paste drift.

**Requirements.** Create standalone components under `frontend/src/shared/components/` (templates in `frontend/src/shared/ui/` following the existing convention). All are presentational, signal-input based (`input()` / `output()`), themed with G-1 tokens:

| Selector | Purpose / API |
|---|---|
| `app-page-header` | `title`, `subtitle?`, optional `<ng-content>` for actions/filters. Replaces the centered `<div class="mb-8 text-center">` block. Left-aligned on `≥ md`, centered on mobile. |
| `app-search-input` | `value` (model), `placeholder`, emits on input (debounce 150 ms), has a visible clear button with `aria-label="Clear search"`, `type="search"`, `<label class="sr-only">`. |
| `app-filter-bar` | Takes `groups: FilterGroup[]` where `FilterGroup = { key: string; label: string; options: { value: string; label: string; count?: number }[] }` and `selected: Record<string,string>`. **Desktop (`≥ md`)**: renders groups inline as chip rows above the results. **Mobile (`< md`)**: renders a "Filters (n)" button that opens `app-bottom-sheet`. Emits `selectedChange`, `clear`. Shows active filters as removable chips (`×`) under the search on all sizes. |
| `app-bottom-sheet` / `app-dialog` | Accessible overlay: `role="dialog" aria-modal="true" aria-labelledby`, focus trap (use `@angular/cdk/a11y` `cdkTrapFocus` — CDK is already a dependency), closes on Escape and backdrop click, restores focus to the trigger, locks body scroll via a class (not inline style), animates with `motion-safe:` only. |
| `app-chip` | Toggle chip: `selected`, `disabled`, `aria-pressed`. 36 px min height on touch. |
| `app-badge` | `variant: 'neutral' \| 'success' \| 'info' \| 'warning' \| 'danger' \| 'accent'`, `size: 'sm' \| 'md'`. Used for all status/level labels. |
| `app-empty-state` | `icon` (Lucide), `title`, `description?`, optional action slot. |
| `app-error-state` | `message`, `retry` output → renders a "Try again" button. |
| `app-skeleton` | Shimmer block with `class` passthrough; plus `app-card-skeleton` preset matching card dimensions. Respects `prefers-reduced-motion` (no shimmer, static block). |
| `app-back-link` | `routerLink`, `label` — consistent `ArrowLeft` back link. |
| `app-breadcrumb` | `items: { label; link? }[]`, `<nav aria-label="Breadcrumb">`, `aria-current="page"` on last. |
| `app-project-card` | Renders a `ProjectListItem`/`ProjectInJob`/`ProjectInVolunteer`-shaped input (image?, title, description, year, status, tags/skills, demo/github links, routerLink to `/projects/:id`). Replaces the three different card markups in projects list, job detail, volunteer detail. |
| `app-timeline` + `app-timeline-item` | Vertical rail with dots/dates; used by Education (exists ad-hoc), Jobs, Volunteer lists. |
| `app-stat` | Big number + label (for hero stats and project metrics). |
| `app-section` | Home page section wrapper: eyebrow label, title, "View all →" link, content slot. |

Also create **pipes/utilities** in `frontend/src/shared/`:
- `enumLabel` pipe: `"IN_PROGRESS" → "In progress"`, `"NGO" → "NGO"` (keep ≤ 3-letter all-caps tokens as-is), with an optional override map. Replace `formatLabel`, `levelLabel`, `statusLabel`, `label()` helpers in skills/education/hobbies pages.
- `statusVariant(status: string): BadgeVariant` mapping: `PRODUCTION|COMPLETED|EXPERT|ONGOING→success`, `DEVELOPMENT|TESTING|IN_PROGRESS|INTERMEDIATE|ADVANCED→info`, `PLANNING|BEGINNER|PAUSED|MAINTENANCE→warning`, `ARCHIVED|DROPPED|NOT_STARTED→danger/neutral`, `LOW/MEDIUM/HIGH → neutral/warning/danger`.
- `duration` util: `(start: string, end?: string|null) → "2 yrs 3 mos"` and `"Present"`.
- `compactNumber` pipe: `1234567 → "1.2M"`, `15400 → "15.4K"`.

### G-3 Loading, error and empty states

**Finding.** Every page shows the literal text `Loading...`; errors are a red sentence with no recovery; the backend can take ~15 s to wake.

**Requirements.**
1. Replace all `Loading...` with `app-skeleton` layouts that mirror the final layout (hero skeleton on home, N card skeletons on lists, header + gallery skeleton on details). Container gets `aria-busy="true"` while loading.
2. Add a global **HTTP slow-request interceptor** (`frontend/src/shared/services/slow-request.interceptor.ts`, registered in `app.config.ts` via `withInterceptors`). If any request is pending for > 3 s, set a signal that renders a dismissible banner under the header: *"The server is waking up — this can take up to 20 seconds on first visit."* Hide when the request completes.
3. Replace red error text with `app-error-state` and a working **Retry** (re-invokes the load method). Error copy must be human: "We couldn't load the projects. Check your connection and try again."
4. Empty states use `app-empty-state`; for filtered-empty results the action is "Clear search and filters".
5. Add a simple in-memory cache (`shareReplay(1)` or a `Map` in each public service) so navigating list → detail → back does not refetch and re-skeleton the list. Cache lifetime: session (no TTL needed).

### G-4 Filtering & search UX

**Finding.** Filters live behind a floating round button (`fixed bottom-6 right-6`) that: is invisible as a concept on desktop, overlaps page content and the footer's "Send message" button on mobile, has no `aria-label`, and opens a non-accessible modal. Filter state resets on navigation and is not shareable. Search and filters are present even on pages with 2–5 items (education, hobbies).

**Requirements.**
1. Remove all floating filter buttons. Use `app-filter-bar` (G-2): inline chips on desktop, bottom sheet on mobile.
2. Show a **result count** next to the filter bar: "12 projects" / "3 of 12 projects" when filtered.
3. Sync `searchTerm` and each active filter to **URL query params** (`?q=angular&status=PRODUCTION`) using `Router.navigate` with `queryParamsHandling: 'merge'` and `replaceUrl: true`; read them on init. Back/forward and sharing must restore state.
4. Filter options should display **counts** (`Production (4)`) computed from the full dataset.
5. Only render the search box when the collection has ≥ 6 items; only render a filter group when it has ≥ 2 distinct values (already partially done).
6. Add **sort** where it matters: Projects (Newest / Oldest / A–Z, default = `sortOrder` then `year desc`), Certificates (Issue date desc default), Skills (Level desc default). Implement as a `<select>` styled like the search input, labelled.

### G-5 Accessibility baseline (WCAG 2.1 AA)

**Findings & requirements** (all mandatory):
1. **Focus visibility.** Add to `styles.css`: `:focus-visible { outline: 2px solid var(--color-ring); outline-offset: 2px; }` and remove any `outline-none` that is not paired with a visible focus ring (search inputs currently use `outline-none` + `focus:ring-2` — acceptable; buttons/links have nothing).
2. **Icon-only controls need names.** Add `aria-label` to: search clear buttons, filter triggers, modal close buttons, gallery thumbnails (`aria-label="Show image 2 of 5"`, `aria-pressed`), header CV icon button (exists), theme toggle (exists), mobile menu button (exists).
3. **Hidden mobile menu** (`#mobile-nav`) is translated off-screen but remains in the tab order. Set `[inert]="!isMenuOpen()"` and `[attr.aria-hidden]="!isMenuOpen()"`, and move focus into it on open / back to the trigger on close.
4. **Dropdown menus** (header Experience/About): add `aria-haspopup="menu"`, `aria-controls`, close on Escape, support `ArrowDown/ArrowUp` to move between items, and `Tab` out closes the menu.
5. **Tabs** (project detail): `role="tablist"`, `role="tab"`, `aria-selected`, `aria-controls`, `role="tabpanel"`, `tabindex` roving, `ArrowLeft/ArrowRight` navigation.
6. **Progress bars** (skill level): `role="progressbar" aria-valuenow aria-valuemin="0" aria-valuemax="100" aria-label="{{skill.name}} level"`.
7. **Live regions.** Footer contact form status messages and the "n results" count get `aria-live="polite"`.
8. **Motion.** Every `hover:-translate-y-*`, `hover:scale-*`, `transition-transform`, shimmer, slide-in must be prefixed `motion-safe:` (or wrapped in `@media (prefers-reduced-motion: no-preference)`).
9. **Touch targets** ≥ 44 × 44 px on mobile for all interactive elements (header icon buttons are 32 px → make `h-10 w-10`; search clear `×` is 16 px → pad to 40 px hit area).
10. **Contrast.** Replace all `/40`, `/50`, `/60` text opacities with `text-fg-muted`/`text-fg-subtle` tokens that pass AA (G-1). Placeholder text ≥ 3:1.
11. **Hover-only affordances.** The "Details" label currently underlines only on hover and uses the *external link* icon for internal navigation. Use `ArrowRight` for internal links and `ExternalLink` only for `target="_blank"`. External links get `rel="noopener noreferrer"` and a visually hidden "(opens in new tab)".
12. **Landmarks & headings.** Each page has exactly one `<h1>`; sections use `<section aria-labelledby>`; add a **"Skip to content"** link as the first focusable element in `app.html` targeting `<main id="main-content">`. Note: `app.html` currently wraps everything in `<main class="main">` including header/footer — restructure to `<header> … <main id="main-content"> <router-outlet/> </main> <footer>` (header/footer components already render `<header>`/`<footer>` elements; make the wrapper a `<div>`).

### G-6 Images & media

**Finding.** `<img>` tags lack `loading="lazy"`, `decoding="async"`, explicit dimensions (layout shift), and error fallbacks. Avatars/projects are remote URLs.

**Requirements.**
1. All non-hero images: `loading="lazy" decoding="async"`; hero avatar: `fetchpriority="high"`.
2. Wrap images in a fixed-aspect container (already `aspect-video` for projects) and give the `<img>` `width`/`height` attributes or `class="h-full w-full object-cover"` inside an aspect box (so CLS = 0).
3. `(error)` handler swaps to the existing icon placeholder (projects) or initials avatar (profile): create `app-avatar` that shows `firstName[0]+lastName[0]` when `imageLink` is empty or fails.
4. Use the Angular `NgOptimizedImage` directive (`ngSrc`) where the URL is known at build time (logo); for API URLs use plain `<img>` with the attributes above.

### G-7 Navigation, routing & shell behaviour

**Findings & requirements.**
1. **No 404 route.** Add `NotFoundPage` (`features/not-found/`) and `{ path: '**', component: NotFoundPage }` as the last route, with SEO `noindex`, a friendly message, and links to Home/Projects.
2. **Scroll position.** In `app.config.ts`: `provideRouter(routes, withInMemoryScrolling({ scrollPositionRestoration: 'enabled', anchorScrolling: 'enabled' }), withViewTransitions())`. View transitions must be disabled under `prefers-reduced-motion` (Angular honours this automatically for the default crossfade; verify).
3. **Theme persistence.** `ThemeService`: read `localStorage['theme']` (`'light' | 'dark' | null`), fall back to `prefers-color-scheme`, listen to `matchMedia` changes while no explicit choice is stored, persist on toggle. Add an **inline script in `index.html` `<head>`** that applies the `dark` class before first paint to avoid a flash. Remove the `ngOnDestroy → toggleTheme()` call and the `console.log` in `app.ts`.
4. **Header height.** The header is 56 px on mobile and 64 px on `≥ sm` (padding + logo size), yet pages subtract `3.5rem`. Set `--header-h: 3.5rem` / `@media (min-width: 640px) { --header-h: 4rem }` on `:root` and use it in the header and all `min-h` calculations (G-1.6).
5. **Page titles** already handled by `SeoService` — keep. Detail pages set their own titles after load — keep.
6. **Route-level loading**: detail pages read `id` once via snapshot; fine. Guard against `NaN` id → navigate to 404.

### G-8 Header (`shared/ui/header.component.html`, `header.component.ts`)

**Findings.**
- Brand text is the literal "Portfolio" (`title = "Portfolio"`); a visitor should see the owner's name.
- Primary actions are **Login / Register** (filled emerald button = strongest visual weight). Visitors never need an account; the valuable CTAs are *Download CV* and *Contact*.
- CV button label is just "CV" on desktop.
- Nav structure is hacked into the `@for` loop (`@if (link.label === "Projects")` injects the Experience dropdown; `@if (link.label === "Skills")` injects About). Two near-identical dropdown implementations.
- Parent dropdown buttons do not show an active state when a child route (`/experience/jobs`) is active.
- Dropdowns are not keyboard accessible (G-5.4).
- Hidden mobile menu remains focusable (G-5.3); body scroll lock uses inline `style.overflow`.
- Nav font `lg:text-lg` is oversized for a top bar and crowds the actions at 1024–1150 px.
- Header icon buttons are 32 px (G-5.9).

**Requirements.**
1. Brand: show the owner's name from the profile when loaded (inject `HomeService`/a shared `ProfileStore`) — fallback "Portfolio" while loading. On `< sm` show initials/logo only.
2. Data-driven nav model: `NavItem = { label; path?; icon; children?: NavItem[] }`; one generic dropdown implementation; parent gets active style when `router.isActive(child.path)` for any child.
3. CTA hierarchy (desktop): `[Download CV]` filled accent button with full label; `[Contact]` ghost button that navigates to `/#contact` (anchor on the footer form, works with `anchorScrolling`). **Login** becomes a small text link; **Register** is removed from the header (keep the route; link to it from the login page). When authenticated, show a compact user menu: *Admin* (if ADMIN) and *Logout*.
4. Mobile menu: same items; put CV + Contact as two full-width buttons at the top of the sheet; Login/Logout as text at the bottom. Add `inert`, focus management, Escape (exists), body-scroll-lock class.
5. Nav text `text-sm font-medium` (≤ 15 px); active link uses an accent underline/indicator, not just a grey background.
6. Collapse to hamburger at `< lg` (1024 px) — keep, but verify the desktop bar fits at exactly 1024 px with the longest brand name without wrapping (reduce gap/padding if needed).
7. Add a thin scroll-progress or elevation change (`shadow-sm` once `scrollY > 8`) so the sticky header separates from content.

### G-9 Footer (`shared/ui/footer.component.html`, `footer.component.ts`)

**Findings.** Contact form inputs rely on placeholders only (no `<label>`), show no validation errors (required fields silently block submit), success/error text is not announced and the form is not reset after success; WhatsApp is the only filled button; GitHub is absent from the footer; no site links; no "back to top".

**Requirements.**
1. Three-column footer on `≥ lg` (stack on mobile): **About** (name, one-line blurb, social icon buttons: GitHub `LucideGithub`, LinkedIn `LucideLinkedin`, Email, WhatsApp if phone), **Explore** (links to all 7 public sections), **Contact form** with `id="contact"` for the header anchor.
2. Form: visible labels, `required`, inline error messages on blur/submit (`name` ≥ 2 chars, valid email, message ≥ 10 chars), submit disabled until valid, loading spinner on submit, success state replaces the form with a confirmation card + "Send another", `aria-live="polite"` status, reset on success. Keep existing `ContactService` and rate-limit handling.
3. Bottom bar: copyright + "Back to top" button (`motion-safe:scroll-smooth`).

### G-10 Auth pages (`/login`, `/register`)

**Findings.** Only these two pages use Angular Material (`mat-card`, `mat-form-field`, `mat-icon` with the Material Icons web font, `indigo-pink` prebuilt theme overridden in `styles.css`). Page background is `beige-light` while every other page uses `beige-dark`, and `min-h` subtracts `4rem` instead of `3.5rem`. The Material Icons stylesheet in `index.html` is loaded for every visitor for one eye icon.

**Requirements.**
1. Rebuild both pages with the shared Tailwind form styles (same input styling as the footer form), Lucide `Eye`/`EyeOff` for the password toggle, `app-badge`/buttons from G-2. Keep the reactive forms, validators, error messages, Google button container (`#googleButton`), and submit/spinner behaviour unchanged.
2. Remove `@angular/material` imports from the two components, the `indigo-pink.css` import and all `.mat-*` overrides from `styles.css`, the Material Icons `<link>` from `index.html`. Then `pnpm remove @angular/material` (keep `@angular/cdk` — used for focus trap). Verify `pnpm build`.
3. Use the shared page background and `--header-h`; card centered, `max-w-md`, with a short explanation above the form: "Accounts are only needed to manage this portfolio." Link login ↔ register.

### G-11 Performance hygiene

1. Self-host fonts or keep Google Fonts but add `<link rel="preload" as="style">` + `font-display: swap` (already `display=swap` in URL) — and drop the Material Icons font (G-10).
2. Lazy-load route components with `loadComponent` in `app.routes.ts` (currently all pages are eagerly imported) — keeps the home bundle small.
3. Avoid refetching (G-3.5).
4. Remove `console.log` from `app.ts`.

---

## 3. Page-by-page findings & requirements

### 3.1 Home `/` — `features/home/ui/home.page.html`

**Current state.** Single centered column: 160 px round avatar, `FirstName LastName`, "N years old", description paragraph, inline contact links (email, phone, location, GitHub and LinkedIn both rendered with a generic `Link` icon). Whole page shows "Loading..." until `/profile/summary` resolves. Nothing invites the visitor further.

**Problems.** No headline/CTA; no preview of content; "years old" is unusual on a professional portfolio; no social icons; blocks on cold start; no hierarchy.

**Requirements** (frontend-only; compose from existing public endpoints: `/profile/summary`, `/projects/public`, skills list, jobs list, certificates list, volunteer list — load each section independently with its own skeleton so one failure does not blank the page; fetch in parallel):

1. **Hero** (`min-h-[calc(100dvh-var(--header-h))]` on desktop, natural height on mobile): two-column on `≥ lg` (text left, avatar right), stacked and centered on mobile.
   - Eyebrow: "Hi, I'm" (small caps, accent).
   - `<h1 class="font-brand text-4xl sm:text-5xl lg:text-6xl">FirstName LastName</h1>`.
   - Sub-headline: the profile `description` (clamp to 3 lines on mobile with a "Read more" toggle if longer than ~280 chars).
   - Location line with `MapPin` (city, country) — age is **not** shown in the hero; if `age` exists, it may appear in a small "About" facts row (optional, default hidden).
   - CTA row: `[View projects]` (filled accent → `/projects`), `[Download CV]` (outline, reuses `CvDownloadService` with the same spinner behaviour as the header), `[Get in touch]` (ghost → `/#contact`).
   - Social icon buttons (40 px, `aria-label`): GitHub (`LucideGithub`), LinkedIn (`LucideLinkedin`), Email (`LucideMail`), Phone (`LucidePhone`) — only those present.
   - Avatar: `app-avatar` 200–280 px with a soft accent ring/gradient blob behind it; `fetchpriority="high"`.
   - Hero skeleton while profile loads (G-3).
2. **Stats strip** (`app-stat` × 4, computed client-side after the parallel fetches resolve; hide any stat whose source failed): `Projects` (count of public projects), `Skills`, `Certificates`, `Years of experience` (now − earliest `startDate` across jobs, rounded down, minimum 1; omit if no jobs).
3. **Featured projects** (`app-section`, title "Selected projects", "View all →" link): first 3 projects ordered by `sortOrder` asc then `year` desc, rendered with `app-project-card`. Section hidden when 0 projects.
4. **Skills snapshot**: top 8–10 skills by `level` (fallback `proficiency` rank EXPERT>ADVANCED>INTERMEDIATE>BEGINNER) as `app-badge`/chips; "See all skills →".
5. **Experience snapshot**: current job (no `endDate`) or most recent job as a highlighted row (role @ company, dates, `duration`), plus the most recent volunteer role; link to each list.
6. **Explore grid**: 7 tiles (Projects, Skills, Jobs, Volunteer, Certificates, Education, Hobbies) with icon, title, one-line description and item count where known. Replaces the need for dropdowns to discover content.
7. **Contact CTA band** above the footer: "Have a project in mind? Let's talk." + button scrolling to `#contact`.
8. Section reveal animation (fade/slide 12 px) using `IntersectionObserver` in a small `appReveal` directive, `motion-safe:` only, SSR-safe (`isPlatformBrowser`).

### 3.2 Projects list `/projects` — `projects-list.page.html`

**Problems.** Status/complexity shown raw (`PRODUCTION`, `INTERMEDIATE`); mobile grid is `grid-cols-2` with description, skills and footer hidden (`hidden md:block`) → tiny cards with only image + `text-sm` title; "Details" uses external-link icon; no result count/sort; filter FAB (G-4); year sits in the title row and wraps awkwardly for long titles; `sortOrder` not used for ordering.

**Requirements.**
1. `app-page-header` with title "Projects", subtitle, and the search + `app-filter-bar` (groups: Category, Status, Complexity — with counts) + sort select (G-4.6) + result count.
2. Default order: `sortOrder` asc (nulls last), then `year` desc, then `title`.
3. Grid: `grid-cols-1 sm:grid-cols-2 xl:grid-cols-3` (max 3 columns — 4 columns at `lg` makes cards too narrow for descriptions). Cards use `app-project-card`:
   - 16:9 image with lazy loading & fallback (G-6); status `app-badge` overlaid top-left on the image with `statusVariant`.
   - Title (`text-lg font-semibold`, clamp 2 lines), year as small muted text under the title (not beside it).
   - Description clamp 2 lines on **all** breakpoints.
   - Up to 4 skill chips + "+N" overflow chip.
   - Footer: complexity label (formatted) left, "View details →" right (`ArrowRight`).
   - Whole card is one `<a>`; inner demo/github links are **not** nested inside the anchor (invalid HTML) — show them only on the detail page.
4. Optional **category tabs** above the grid when ≤ 6 categories (same data as the Category filter; mutually synced).
5. Skeleton grid of 6 cards while loading.

### 3.3 Project detail `/projects/:id` — `project-detail.page.html`

**Problems.** Enums raw (status, complexity, difficulty); gallery has no lightbox/keyboard support and thumbnails lack labels; tabs are not accessible and tab strip overflows on mobile without any indicator; metrics show raw numbers; achievements hide most of their fields (`achievementType`, `achievementDate`, `recognitionLevel`, `recognizedBy`, `proofUrl`, `metricBefore/After`); features ignore `implementationDate`/`developmentTimeHours`; tech tab ignores skill `proficiency`/`usage`; "Xh development" has no icon; `Tags` as a separate tab is low-value; no breadcrumb; no previous/next project navigation.

**Requirements.**
1. `app-breadcrumb`: Home / Projects / {title}.
2. Header block (two columns on `≥ lg`): gallery left, title/meta right (as now) with:
   - `app-badge` status (`statusVariant`) + complexity badge, both formatted via `enumLabel`.
   - Meta chips with icons: `Calendar` year, `Tag` category, `Clock` "{developmentTime} h of development", `Layers` complexity.
   - Tags rendered as small outline chips **under the description** (remove the Tags tab).
   - CTA buttons: Live demo (filled), Source code (outline, `LucideGithub`); on mobile these two become a sticky bottom bar (`sticky bottom-0`, `safe-area-inset-bottom` padding) while the page is scrolled.
3. Gallery: main image `aspect-video rounded-2xl`; thumbnails as a horizontally scrollable strip with `aria-label`/`aria-pressed`; `ArrowLeft/ArrowRight` keys change the image when the gallery has focus; clicking the main image opens a lightbox (`app-dialog`) with prev/next and the media `title`/`description` caption. Show `media.title` as caption under the main image when present.
4. Tabs → accessible tablist (G-5.5). Tab order: Overview, Features (n), Challenges (n), Tech stack (n), Metrics, Achievements (n). Show counts in tab labels. On `< sm`, render tabs as a horizontally scrolling strip with fade masks on both edges to signal overflow. Persist the active tab in `?tab=` query param.
5. **Overview**: `longDescription` with `whitespace-pre-line`, `text-base leading-relaxed`, max-width `prose`-like 70ch.
6. **Features**: cards with title, description, and a meta line: `implementationDate | date:'MMM y'` and `developmentTimeHours h` when present. Sort by `sortOrder`.
7. **Challenges**: difficulty `app-badge` via `statusVariant` (LOW→neutral, MEDIUM→warning, HIGH→danger); description; solution in a highlighted "Solution" block with a `Lightbulb` icon.
8. **Tech stack**: group skills by `proficiency` or show each as a chip with a small proficiency label; if `usage` present, render a thin bar (`role="progressbar"`).
9. **Metrics**: `app-stat` grid; numbers through `compactNumber`; `testCoveragePercentage` with `%`; hide the tab entirely if all metric fields are null.
10. **Achievements**: card with `achievementType` badge (formatted), `achievementDate | date:'MMM y'`, `recognitionLevel` badge, "Recognized by {recognizedBy}", optional `proofUrl` link ("View proof", external), optional before/after metric pair (`metricBefore → metricAfter`), `isFeatured` gets an accent border + `Star` icon.
11. **Prev / next project** footer: fetch the public list (cached — G-3.5), find the neighbours in default order, render two cards "← Previous: {title}" / "Next: {title} →".
12. Not-found error → `app-error-state` with "Back to projects".

### 3.4 Skills `/skills` — `skills-list.page.html`

**Problems.** Flat 3-column grid of same-sized cards is hard to scan when there are many skills; the category is only a tiny caption; proficiency badge and level bar say the same thing twice; "yrs experience" abbreviation; flags (Certified/Learning) are low-visibility; no progressbar ARIA; local `formatLabel`.

**Requirements.**
1. Group skills **by `categoryName`** (uncategorised last as "Other"): each group is a section with an `h2`, count, and a responsive grid `grid-cols-1 sm:grid-cols-2 lg:grid-cols-3`. On mobile groups are collapsible (`<details>`/accordion, first group open).
2. Card content: name (`text-base font-semibold`), proficiency `app-badge` (`statusVariant`), level bar with ARIA (G-5.6) and the numeric value as `sr-only`; one meta line: "{years} years" (full word), `Award` "Certified", `GraduationCap` "Currently learning" (as badges, not plain text), `lastUsedDate` as "Last used MMM y" if present; description clamp 2 lines.
3. Add a **proficiency legend** (4 dots/colours + labels) under the page header.
4. Search (≥ 6 skills), filters Category/Proficiency via `app-filter-bar`, sort by Level desc default (G-4). Keep a "Learning now" quick filter chip when any skill has `learning === true`.
5. Replace `formatLabel` with `enumLabel` pipe.

### 3.5 Jobs list `/experience/jobs` — `jobs-list.page.html`

**Problems.** Three-column card grid for a chronological list hides the story; no total duration per role; `statusOf(job)` ("Ongoing"/"Completed") fine but the filter sheet is overkill for one group; "Details" external icon; filter FAB.

**Requirements.**
1. Render as a **vertical timeline** (`app-timeline`), newest first (sort by `startDate` desc, ongoing roles first). Each item: date range (`MMM y – MMM y | Present`) + `duration` ("1 yr 4 mos") as the rail label; card with company (`h2`), role, `app-badge` Ongoing/Completed, "{projectsCount} projects" with `FolderKanban`; whole card links to the detail with "View role →".
2. Current role gets an accent dot + pulsing ring (`motion-safe:animate-ping`).
3. Filters: single inline segmented control **All / Ongoing / Completed** (no sheet); search only when ≥ 6 jobs.
4. Summary line under the header: "{n} roles · {total years} years in total" (sum of durations).

### 3.6 Job detail `/experience/jobs/:id` — `job-detail.page.html`

**Problems.** Very sparse (only company, role, dates, count, linked projects); pervasive `text-xs`; page padding `py-6` inconsistent; project mini-cards duplicate volunteer's; "View project details" with external icon; no prev/next.

**Requirements.**
1. `app-breadcrumb`: Home / Experience / Jobs / {company}.
2. Header card: `Briefcase` avatar, `h1` "{role}", `h2`-style company line, badge Ongoing/Completed, date range + `duration`, "{n} projects". Minimum `text-sm`.
3. Projects section uses `app-project-card` (compact variant, no image) in `grid-cols-1 md:grid-cols-2`; status badge formatted; demo/github icons as secondary links; "View project →".
4. Prev/next role navigation at the bottom (uses the cached jobs list ordered as the timeline).
5. Unify page padding to G-1.4.

### 3.7 Volunteer list `/experience/volunteer` — `volunteer-list.page.html`

**Problems.** Same as jobs plus raw enums `type` (`NGO`, `ASSOCIATION`) and `status` (`COMPLETED`).

**Requirements.** Same timeline treatment as 3.5; `type` as a neutral `app-badge` via `enumLabel` (keep "NGO" uppercase), `status` via `statusVariant`; location with `MapPin`; inline filters: Type chips + status segmented control.

### 3.8 Volunteer detail `/experience/volunteer/:id` — `volunteer-detail.page.html`

**Problems.** Left column crammed with `text-xs`; `impactLevel` raw; `contributionPercentage` shown as a bare "40%"; responsibilities list dense; duplicate project cards.

**Requirements.**
1. Breadcrumb; header card as in 3.6 (`HeartHandshake` avatar, organization `h1`, role, type + status badges, dates + duration, location, `hoursPerWeek`/`totalHours` as `app-stat` mini-tiles, Website button).
2. "Impact" callout: accent left border, `Sparkles` icon, `text-base`.
3. Responsibilities: ordered by `sortOrder`, each row `text-sm` with an `impactLevel` badge (`statusVariant`).
4. Projects: `app-project-card` compact variant with an extra "Contribution" row: label + thin progress bar + "{n}%".
5. Prev/next navigation.

### 3.9 Certificates `/certificates` — `certificates-list.page.html`

**Problems.** Generic award icon on every card; `credentialId` and `score` never shown; expired state is only a red date; no sort; filter FAB.

**Requirements.**
1. Default sort `issueDate` desc; sort select (Newest / Oldest / A–Z).
2. Card: provider "logo" = initials avatar (first letters of `provider`, deterministic background hue from a hash of the provider name) instead of the generic icon; name (`h2`, clamp 2), provider; badges: Verified (`BadgeCheck`, success), category (neutral), **Expired** (danger) when `isExpired`; expired cards get `opacity-75`; meta: "Issued MMM y", "Expires MMM y"/"Expired MMM y", `score` as "Score: {score}", `credentialId` as a copyable monospaced chip with a `Copy` button (`navigator.clipboard`, toast "Copied") when present; description clamp 3; "View credential ↗" external link.
3. Filters Category/Provider inline; a "Valid only" toggle chip hides expired certificates.
4. Optional grouping by year with sticky year headings on `≥ md`.

### 3.10 Education `/education` — `education-list.page.html`

**Current state.** Already a timeline — the best page; reuse its pattern via `app-timeline`.

**Problems.** `hover:-translate-x-1` moves the card toward/over the rail; local `levelLabel/statusLabel`; "GPA:" raw; search + filter sheet for a handful of entries; DROPPED status only differs by red.

**Requirements.**
1. Migrate to `app-timeline` (shared with jobs/volunteer); newest first; remove the translate hover (use shadow/border only).
2. Badges via `enumLabel` + `statusVariant` (DROPPED → danger, IN_PROGRESS/ongoing → info, COMPLETED → success).
3. "GPA {gpa}" as an `app-stat` mini-tile; `fieldOfStudy` shown as subtitle when both degree and field exist.
4. Filters inline (Level chips, Status segmented); search only when ≥ 6 entries.

### 3.11 Hobbies `/hobbies` — `hobbies-list.page.html`

**Problems.** Every hobby uses the same `Heart` icon; "yrs" abbreviation; most "serious" card styling for a light-hearted section; filter sheet overkill.

**Requirements.**
1. Map `category → Lucide icon`: LEARNING→`GraduationCap`, SPORTS→`Dumbbell`, CREATIVE→`Palette`, SOCIAL→`Users`, TECHNOLOGY→`Cpu`, MUSIC→`Music`, TRAVEL→`Plane`, COOKING→`ChefHat`, GARDENING→`Sprout`, READING→`BookOpen`, GAMING→`Gamepad2`; each category also gets a soft accent colour for the icon tile (deterministic map, both themes).
2. Layout: responsive grid `grid-cols-1 sm:grid-cols-2 lg:grid-cols-3`, cards with the coloured icon tile, name, category label, `activityLevel` badge (OCCASIONAL→neutral, REGULAR→info, FREQUENT→success, DAILY→accent), description clamp 3, `favoriteAspect` as a quote block (`Quote` icon), meta "{yearsActive} years", complexity label.
3. Filters: inline Category chips (with icons); search only when ≥ 6 hobbies.

### 3.12 Login & Register — see G-10.

### 3.13 Not found `**` — see G-7.1.

---

## 4. Responsiveness requirements (test matrix)

Verify every page at these viewport widths (Chrome DevTools device toolbar), in **light and dark** mode, with **no horizontal scrollbar** and all tap targets ≥ 44 px:

| Width | Represents | Specific checks |
|---|---|---|
| 360 px | small Android | Header fits (logo + CV + theme + menu) without wrap; hero CTAs stack full-width; filter bottom sheet; cards 1 column; detail sticky CTA bar; footer stacks; tab strips scroll with edge fades |
| 390 px | iPhone 14 | same as 360 |
| 768 px | tablet portrait | 2-column grids; hamburger still shown (nav collapses `< lg`); filter chips inline (`≥ md`); hero still stacked |
| 1024 px | tablet landscape / small laptop | Desktop header appears — no overflow with the longest brand name; hero 2 columns; 3-column grids |
| 1280 px | laptop | Max content width `max-w-7xl` (lists) / `max-w-6xl` (detail/home) centered |
| 1536 px+ | desktop | No stretched cards; keep max 3 columns for cards |

Additional rules:
- Use `100dvh` (not `100vh`) for full-height sections to account for mobile browser chrome.
- Respect `env(safe-area-inset-bottom)` on sticky bottom bars/sheets.
- Long unbroken strings (URLs, tags) must `break-words`/`truncate`.
- Images never overflow their container (`max-w-full`).
- Dropdown menus/sheets must not be clipped by `overflow-hidden` ancestors.

---

## 5. Implementation phases (suggested order)

Each phase should end with `pnpm build` passing and a manual pass of §4 for the touched pages.

### Phase 1 — Foundation (enables everything else)
1. G-1 tokens in `styles.css` (+ dark overrides), focus-visible rule, `--header-h`, `motion-safe` policy.
2. G-2 shared components, `enumLabel`/`compactNumber` pipes, `statusVariant`, `duration` util.
3. G-3 skeletons, `app-error-state` with retry, slow-request interceptor + banner, in-memory service cache.
4. G-7 routing: 404 page, scroll restoration, view transitions, theme persistence + inline no-flash script, remove `console.log`, lazy routes (G-11.2).
5. G-5.12 shell restructure (`<main id="main-content">`, skip link).

### Phase 2 — Shell
6. G-8 header rework (brand name, nav model, CTA hierarchy, a11y, 44 px targets, inert mobile menu).
7. G-9 footer rework (3 columns, labelled validated form, `#contact` anchor, socials, back-to-top).

### Phase 3 — Home page (highest visitor impact)
8. §3.1 hero, stats, featured projects, skills snapshot, experience snapshot, explore grid, contact band, reveal directive.

### Phase 4 — List pages
9. §3.2 Projects list (first — defines `app-project-card` and `app-filter-bar` usage, URL-synced filters, sort).
10. §3.5/3.7/3.10 Jobs, Volunteer, Education onto `app-timeline`.
11. §3.4 Skills grouped by category; §3.9 Certificates; §3.11 Hobbies.

### Phase 5 — Detail pages
12. §3.3 Project detail (accessible tabs, lightbox, richer sections, prev/next).
13. §3.6/3.8 Job and Volunteer detail (shared header card, compact project cards, prev/next).

### Phase 6 — Auth & cleanup
14. G-10 rebuild login/register without Material; remove `@angular/material`, Material Icons link, `.mat-*` CSS.
15. Final §4 matrix pass, Lighthouse (Performance ≥ 90, Accessibility ≥ 95, Best Practices ≥ 95, SEO ≥ 95 on `/` and `/projects`), axe DevTools zero critical issues.

---

## 6. Definition of done (checklist for reviewers)

- [ ] No raw enum strings visible anywhere on public pages (search templates for `{{ p.status }}`, `{{ item.type }}`, etc. without a pipe).
- [ ] No literal `Loading...` text; all loading states are skeletons with `aria-busy`.
- [ ] Every error state has a working Retry.
- [ ] No `fixed bottom-6 right-6` filter buttons remain; filters are inline (desktop) / bottom sheet (mobile) and reflected in the URL.
- [ ] Every icon-only button has an `aria-label`; `Tab` never lands on hidden UI; dialogs trap focus and close on Escape.
- [ ] `:focus-visible` ring visible on all interactive elements in both themes.
- [ ] All motion is `motion-safe:`; page usable with "Reduce motion" enabled.
- [ ] Text contrast ≥ 4.5:1 for body/metadata text in both themes (spot-check 5 muted texts with a contrast tool).
- [ ] Theme choice survives reload with no flash; system preference respected when nothing stored.
- [ ] Navigating to a detail page scrolls to top; browser back restores list scroll and filters.
- [ ] Unknown URL shows the 404 page.
- [ ] `@angular/material` and the Material Icons stylesheet are gone; `pnpm build` succeeds (SSR).
- [ ] Home page shows hero + stats + featured projects + skills + experience + explore + contact band, each section degrading gracefully if its request fails.
- [ ] §4 viewport matrix passes with no horizontal overflow at 360 px.
- [ ] Header shows the owner's name, "Download CV" and "Contact" as primary actions; "Register" is not in the header.
