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


Version 2:
- multi user
- allows to work offline with timers. Syncs automatically with server once connection is established again. Smart resolution of conflicts. In case smart resolution is not possible, ask user to resolve the conflicts

## Status

V1 MVP scope (Timer, Projects, Clients, Tags — see PLAN.md §1) has been
scaffolded end to end: Gradle/Compose project, Login Flow v2 auth,
Retrofit networking against the existing `AjaxController` endpoints (no
backend changes), and all four screens with their ViewModels.

**This has not yet been compiled.** The sandbox this was built in has no
x86_64 emulation available for Android's build-tools (`aapt2`/`d8` are
x86_64-only Linux binaries; the host is arm64 with no binfmt_misc/QEMU
support), so nothing here has gone through `./gradlew build` yet. Version
pins in `gradle/libs.versions.toml` were chosen for verified real-world
compatibility (Kotlin 2.0.21 + KSP 2.0.21-1.0.28 + AGP 8.7.3 + Compose BOM
2024.12.01) rather than "latest" for exactly this reason — reducing the
odds of an obscure tooling mismatch that couldn't be caught here.

**First step on a machine with a real Android SDK (or via the `ci.yml`
GitHub Actions workflow, which runs on real x86_64 runners):**

```
./gradlew ktlintCheck detekt assembleDebug testDebugUnitTest
```

Expect this first run to surface real build errors — Kotlin/Compose/Hilt
API usage was written from careful review, not from a passing compile —
and treat that first CI run as the actual verification gate this plan
couldn't provide. The codebase was, however, checked for the concrete
things that could be verified without compiling: every `R.string.*`
reference used in code has a matching entry in `strings.xml` (both
locales), the source tree was scanned for stray non-ASCII/corrupted
identifiers, and all AjaxController endpoint contracts (paths, params,
response shapes) were cross-checked against the actual PHP controller
source, not assumed from memory.

## Known V1 MVP limitations (by design, not oversights)

- Files-app SSO (`Android-SingleSignOn`) isn't implemented yet — only the
  standalone Login Flow v2. See PLAN.md §3/§9.
- Projects' "Allowed tags" / "Allowed users" (for locked projects) aren't
  exposed in the Android UI yet — full CRUD (name/color/client/locked/
  archived/delete) is. This needs a proper multi-select component plus the
  OCS users API; deferred rather than rushed.
- The Timer's inline tag picker shows all tags regardless of whether the
  entry's project is locked (the backend still correctly restricts what
  actually gets saved either way — see `TimerRepository.kt`).
- No dynamic per-server theming yet (colors are a static Nextcloud-blue
  palette matching the default Files app theme, not each server's
  configured theming colors from the capabilities API).
