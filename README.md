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
are all implemented — Gradle/Compose project, Login Flow v2 auth (plus
Files-app SSO via `Android-SingleSignOn`, see below), Retrofit networking
against the existing `AjaxController` endpoints (no backend changes),
dynamic per-server theming, and admin-gating for admin-only features
(locked-project allowed-tags/users, Timelines Admin), matching the Vue
frontend's own `isAdmin` checks. A thin Room-backed response cache for
Projects/Clients/Tags (PLAN.md §4) falls back to the last-known list if
the server's unreachable, rather than leaving pickers empty.

Every layer has unit test coverage (ViewModels, repositories, the Login
Flow v2 polling loop, the Room cache's fallback behavior) plus an
instrumented Compose UI test for the Timer start/stop critical path — see
"Testing" below. A `release.yml` GitHub Actions workflow builds a signed
release APK from a tagged push and attaches it to a GitHub Release, with
an optional (secret-gated) Play Store upload job; `fastlane/metadata/`
holds the store listing text both F-Droid and Play consume.

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

## Testing

- `./gradlew testDebugUnitTest` — ViewModels/repositories/AuthRepository's
  Login Flow v2 polling loop/Room cache fallback, all against hand-written
  fakes (`app/src/test/.../fakes/`) rather than a reflection-based mocking
  library — MockK's kotlin-reflect bootstrap proved unusably slow in this
  project's build environment (multi-minute hangs, eventually an OOM),
  and a plain fake has none of that cost.
- `./gradlew connectedDebugAndroidTest` — one instrumented Compose UI test
  (`TimerScreenTest`) drives the real production Composable/ViewModel/
  repository stack through starting and stopping a timer, with only the
  network interface faked; needs a connected device/emulator.

## Known V1 MVP limitations (by design, not oversights)

- The Timer's inline tag picker shows all tags regardless of whether the
  entry's project is locked (the backend still correctly restricts what
  actually gets saved either way — see `TimerRepository.kt`). The web
  frontend has the same behavior (its `TimerView.vue` doesn't filter by
  locked-project either), so this matches parity rather than falling short
  of it — full client-side filtering would need the backend to expose a
  locked project's allowed tags to non-admin users, which no existing
  endpoint does.
- Files-app SSO only covers the case where the Files app is already
  installed and the user grants account access on the first try; the
  "permission not yet granted" retry path (`AndroidGetAccountsPermission-
  NotGranted`) surfaces a message asking the user to try again rather than
  automatically retrying the account picker itself.
- The Play Store upload job in `release.yml` needs a `PLAY_SERVICE_ACCOUNT
  _JSON` secret and the `PLAY_PUBLISHING_ENABLED` repo variable set before
  it does anything — until then it's skipped (not failed), and GitHub
  Releases + F-Droid still ship from the same tag on their own. Submitting
  this app to F-Droid's own `fdroiddata` repo (a recipe pointing at this
  repo) is a separate, one-time action for the project owner to take; this
  repo's `fastlane/metadata/` is what that recipe would read from.

Since the last update, several items formerly listed here have been
closed: Projects' "Allowed tags" / "Allowed users" editing for locked
projects is in the Android UI (admin-only, matching `ProjectsView.vue`'s
own `isAdmin` gating), the app fetches the server's actual theming color
from the capabilities OCS endpoint instead of a static palette, automated
tests exist (see "Testing" above), a thin Room response cache backs
Projects/Clients/Tags, Files-app SSO is implemented, and a release
pipeline builds signed APKs from tags.
