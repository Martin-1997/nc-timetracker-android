# Time Tracker for Android — V1 Plan

This document plans the first version of the native Android client for the
[Nextcloud Time Tracker app](https://github.com/mtierltd/timetracker),
covering architecture, auth, API integration, scope, and rollout.

Decisions already made with the project owner are marked **(confirmed)**.
Everything else is a recommendation open to override.

## 1. Scope of V1.0

**(confirmed)** V1.0 ships an MVP, not full parity:

- **Timer**: start/stop, resume, edit name/details, edit start/end time,
  delete, inline project + tag assignment, cost entry, manual entry, date
  range with quick-range presets
- **Projects**: CRUD, color, client assignment, locked/archived flags
  (admin-only fields where applicable)
- **Clients**: CRUD
- **Tags**: CRUD, plus inline tag creation from the Timer's tag picker

Deferred to v1.1+ (still "online only / single user", just later,
no architecture change needed — same backend, same auth, just more screens):
Goals, Reports, Dashboard, Timelines, Timelines Admin.

Rationale: Timer+Projects+Clients+Tags is the complete daily-use loop (you
can start tracking time against a real project/client/tag set on day one),
while Reports/Dashboard/Timelines are analysis/export features layered on
top of the same data and can follow without touching the app's foundations.

## 2. Backend integration — verified, no server changes needed

**Key finding, verified empirically against a live Nextcloud 34 instance
running the current timetracker app**: every `AjaxController` endpoint —
including the 30 of 41 methods *not* marked `@NoCSRFRequired` — is reachable
via plain HTTP Basic Auth with a Nextcloud app password, with **no CSRF
token required**. Confirmed by calling `POST /apps/timetracker/ajax/add-tag/{name}`
(no `@NoCSRFRequired` annotation) via `curl -u user:app-password` with zero
CSRF handling and getting a normal 200 response.

This means: **the Android app talks to the exact same endpoints the Vue
frontend does, over Basic Auth, with zero backend modifications.** No new
OCS API, no controller changes, no migration. The full request/response
contract for every endpoint the MVP needs (start-timer, stop-timer,
work-intervals, update-work-interval, add-work-interval, delete-work-interval,
add-cost, projects/projects-table, add/edit/delete-project(-with-data),
clients and tags CRUD) is already documented from the Vue migration and can
be handed directly to the Android networking layer — no reverse-engineering
required.

Two things worth validating early in implementation (cheap to check, not
blocking the plan):
- Confirm the same holds for the `OC-APIRequest`/`OCS-APIRequest` header
  convention Nextcloud expects on some routes (harmless to always send it).
- Some `@NoAdminRequired` methods still depend on `\OC_User::isAdminUser()`
  for behavior branching (e.g. `getProjectsTable`, `editProject`'s locked/
  allowedTags/allowedUsers fields) — this resolves correctly for the
  authenticated app-password user same as it does for session logins, no
  special handling needed.

## 3. Authentication

The README asks for both "Nextcloud Files App" and "Browser Login Flow"
auth. These map to two real, different mechanisms:

- **Nextcloud Files App auth** = the official
  [`Android-SingleSignOn`](https://github.com/nextcloud/Android-SingleSignOn)
  library. It **requires the Nextcloud Files app to be installed** — there
  is no standalone fallback inside that library itself; without Files
  installed it throws `NextcloudFilesAppNotInstalledException`.
- **Browser Login Flow** = Nextcloud's
  [Login Flow v2](https://docs.nextcloud.com/server/latest/developer_manual/client_apis/LoginFlow/index.html)
  protocol, implemented directly: POST to `/index.php/login/v2`, open the
  returned `login` URL in a Custom Tab, poll `poll.endpoint` with
  `poll.token` until it returns `{server, loginName, appPassword}`, store
  the app password.

**Recommendation**: build the standalone Login Flow v2 path first (it's the
one that works unconditionally — no dependency on another app being
installed, and it's the same mechanism the SSO library uses under the hood
against the Files app instead of the server directly). This is also the
better default for GrapheneOS, where users frequently run a minimal app set
and may not have Files installed. Layer in Files-app SSO detection
(`Android-SingleSignOn`) as a v1.x convenience — if installed, offer
"Sign in via Nextcloud Files app" as a one-tap option; otherwise fall back
to the browser flow automatically. Both paths converge on the same
internal credential type (server URL + username + app password), so the
rest of the app (networking, storage) doesn't care which one was used.

**Storage**: app password in `EncryptedSharedPreferences` (AndroidX
Security-Crypto, backed by Android Keystore) — no custom crypto, no
Google-proprietary APIs, works identically on GrapheneOS.

## 4. Tech stack

| Concern | Choice | Why |
|---|---|---|
| Language | Kotlin | standard for new Android apps |
| UI | **Jetpack Compose** (confirmed) | less boilerplate, easier dynamic Material theming from NC's capabilities colors than XML/View-based theming |
| Architecture | MVVM + Repository, unidirectional data flow | standard, testable, works naturally with Compose state |
| Async | Kotlin Coroutines + Flow | no RxJava needed |
| Networking | Retrofit + OkHttp, `kotlinx.serialization` for JSON | OkHttp interceptor adds the Basic Auth header once; no dependency on the Files app or Play Services |
| DI | Hilt | standard, compile-time safe |
| Local storage | Room, used **only** for a thin response cache (matches "online only... except maybe caching") | no offline queue/sync logic in V1 — that's V2 |
| Credential storage | AndroidX Security-Crypto (`EncryptedSharedPreferences`) | Keystore-backed, no Google account dependency |
| Min SDK | 26 (Android 8.0) | covers all GrapheneOS-supported hardware (GrapheneOS only ships for devices from the Pixel 6 era or later, all far above API 26) and the overwhelming majority of stock Android devices still in use |
| Target/compile SDK | latest stable at build time | |
| Static analysis | ktlint + detekt | matches the discipline already established in the backend's CI (PHPStan/PHP lint) |
| **No dependency on**: Firebase/FCM, Play Services SDK, Play Billing, Google Sign-In | | hard requirement for GrapheneOS; also means the app behaves identically regardless of whether Play Services (sandboxed or absent) exists on the device |

## 5. UI / design

- Match the Files app's navigation drawer structure and Material theming:
  fetch the server's theming colors from the capabilities endpoint
  (`ocs/v1.php/cloud/capabilities`, `theming` section — primary color,
  background, etc., same data source the Vue frontend's NcAppNavigation
  gets automatically from Nextcloud core) and apply as a dynamic Compose
  Material color scheme.
- Navigation drawer items mirror the current web sidebar order for the
  in-scope views: Timer, Projects, Clients, Tags (Dashboard/Goals/Reports/
  Timelines/Timelines Admin appear once their v1.x releases land, so the
  drawer structure is stable and just gains entries over time).
- Localization: the backend ships German strings (`info.xml` description
  is bilingual); plan for `strings.xml` + `values-de/strings.xml` from the
  start rather than retrofitting later.

## 6. Distribution

**(confirmed)** Both F-Droid + GitHub Releases, and Google Play, in
parallel.

- Reproducible/deterministic build config from day one (matters for
  F-Droid inclusion and for privacy-conscious GrapheneOS users to verify
  the APK matches source).
- Because there's no Play Services dependency anywhere (see §4), the same
  APK can ship to both channels unmodified — no Play-specific flavor needed.
- Signing: a release keystore kept outside the repo (GitHub Actions secret),
  documented key-rotation/backup process in the repo's `SECURITY.md` or
  `CONTRIBUTING.md` once set up.

## 7. CI/CD

GitHub Actions, mirroring the discipline already in place on the backend
repo:
- Lint (ktlint + detekt) on every PR
- Unit tests (ViewModels, repository layer, Login Flow v2 polling logic)
- Instrumented/UI tests for the critical path (start/stop timer) on a
  couple of API levels via `gradle-managed-devices` or an emulator matrix
- Debug APK artifact on every PR for manual sideload testing
- Release workflow: tag → signed release APK → GitHub Release + F-Droid
  metadata update + Play Store upload

## 8. Milestones

1. **Project scaffold**: Gradle/Compose setup, Hilt, Retrofit client
   pointed at a configurable server URL, empty navigation shell matching
   Files app drawer styling, CI skeleton.
2. **Auth**: standalone Login Flow v2 (server URL entry → Custom Tab →
   poll → store app password → authenticated session). This unblocks
   everything else.
3. **Timer MVP**: list work intervals for a date range, start/stop/resume,
   inline project + tag assignment, cost entry, delete — the daily-use
   loop end to end.
4. **Timer polish**: manual entry, edit name/details/time, date-range
   presets — full parity with the just-shipped web Timer view.
5. **Projects / Clients / Tags**: CRUD screens, reusing the same
   repository/networking patterns established for Timer.
6. **Distribution pipeline**: signing, F-Droid metadata, Play Store listing,
   release workflow — done once, before the actual v1.0 tag.
7. **v1.0 tag** once milestones 1–6 are done and manually verified against
   a real Nextcloud instance (ideally also tested on a real GrapheneOS
   device, not just an emulator, before the public release).

## 9. Decisions confirmed by the project owner

- **UI toolkit**: Jetpack Compose (see §4).
- **Files-app SSO**: treated as a v1.x nice-to-have layered onto the
  standalone Login Flow v2, not a hard v1.0 requirement (see §3). V1.0
  ships with the standalone browser flow only.
- **Multi-instance/account**: V1 is single account + single server; re-auth
  is required to switch. Multi-account/multi-user is a V2 concern.
- **App icon / branding**: reuse the web app's `img/app.svg` as the starting
  point for the Android launcher icon (adapted to adaptive-icon format).

## 10. Implementation status

The MVP scope (§1) has been scaffolded end to end — see the repo root
README's "Status" section for exactly what's built, what's verified
without compiling (string resources, endpoint contracts against the actual
PHP source), and what the first real compile (via `ci.yml` on GitHub's
x86_64 runners) still needs to catch.
