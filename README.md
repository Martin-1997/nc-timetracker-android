# Time Tracker for Android

Native Android client for the [Nextcloud Time Tracker app](https://github.com/mtierltd/timetracker).
Built to work on stock Android and GrapheneOS alike — no Google Play
Services, Firebase/FCM, or Google Sign-In dependency anywhere in the stack.

See [PLAN.md](PLAN.md) for the full V1 architecture/scope plan.

# Planned features

- Auth using either the Nextcloud Files App or the Browser Login Flow
- Same Sidebar and design as the Nextcloud Files App
- Has same features as https://github.com/mtierltd/timetracker/tree/master


Version 1:
- online only: does not store data locally except maybe caching and the login tokens.
- single user

## Status

Full feature parity with the web frontend has been reached: the original
V1 MVP scope (Timer, Projects, Clients, Tags) plus every screen PLAN.md §1
deferred to v1.1+ (Goals, Reports, Dashboard, Timelines, Timelines Admin)
are all implemented — Gradle/Compose project, Login Flow v2 auth,
Retrofit networking against the existing `AjaxController` endpoints (no
backend changes), dynamic per-server theming, and admin-gating for
admin-only features (locked-project allowed-tags/users, Timelines Admin),
matching the Vue frontend's own `isAdmin` checks.

**Compiles, installs, and has been manually verified against a live
Nextcloud test instance.** `./gradlew ktlintCheck detekt assembleDebug
testDebugUnitTest` passes in CI, and `./gradlew assembleRelease` (with a
keystore configured — see `keystore.properties.example`) has been verified
to build and sign locally, though CI does not run it. Every screen has
been installed on a real device and exercised end to end against a real
Nextcloud server over wireless ADB, including cross-device sync (a timer
started on one client and stopped on another correctly reflects on both
without a manual refresh) and file export via the system Sharesheet.
Version pins in `gradle/libs.versions.toml` were chosen for verified
real-world compatibility (Kotlin 2.0.21 + KSP 2.0.21-1.0.28 + AGP 8.7.3 +
Compose BOM 2024.12.01) rather than "latest".

The codebase was also checked for the concrete things that could be
verified without compiling: every `R.string.*`
reference used in code has a matching entry in `strings.xml` (both
locales), the source tree was scanned for stray non-ASCII/corrupted
identifiers, and all AjaxController endpoint contracts (paths, params,
response shapes) were cross-checked against the actual PHP controller
source, not assumed from memory.

## Known V1 MVP limitations (by design, not oversights)

- Files-app SSO (`Android-SingleSignOn`) isn't implemented yet — only the
  standalone Login Flow v2. This is intentionally deferred to v1.x, not a
  v1.0 requirement — see PLAN.md §3/§9.
- The Timer's inline tag picker shows all tags regardless of whether the
  entry's project is locked (the backend still correctly restricts what
  actually gets saved either way — see `TimerRepository.kt`). The web
  frontend has the same behavior (its `TimerView.vue` doesn't filter by
  locked-project either), so this matches parity rather than falling short
  of it — full client-side filtering would need the backend to expose a
  locked project's allowed tags to non-admin users, which no existing
  endpoint does.

- No automated tests yet — `app/src/test`/`app/src/androidTest` are both
  empty. PLAN.md §7 calls for unit tests (ViewModels, repository layer,
  Login Flow v2 polling) and instrumented UI tests for the critical path;
  everything so far has instead been verified by manual live testing
  against a real Nextcloud server (see above). This is the biggest gap
  before a v1.0 tag, per PLAN.md §8 milestone 7.
- No release/distribution pipeline yet — PLAN.md §6 calls for F-Droid
  metadata, a Play Store listing, and a tag → signed release → GitHub
  Release/F-Droid/Play upload workflow. `ci.yml` currently only lints,
  runs unit tests, and uploads a debug APK artifact; there's no
  `release.yml`.
- The Room dependency is declared (`room-runtime`/`room-ktx`/
  `room-compiler`) but nothing uses it yet — no `@Entity`/`@Dao` exists.
  PLAN.md §4 scopes this to "a thin response cache" and the top-level
  scope note already hedges with "except maybe caching", so this isn't a
  hard requirement, just an unclaimed option.

Since the last update, two items formerly listed here have been closed:
Projects' "Allowed tags" / "Allowed users" editing for locked projects is
now in the Android UI (admin-only, matching `ProjectsView.vue`'s own
`isAdmin` gating — see `UsersRepository.isCurrentUserAdmin()`), and the
app now fetches the server's actual theming color from the capabilities
OCS endpoint instead of a static Nextcloud-blue palette (`ThemeRepository`,
applied in `Theme.kt`).
