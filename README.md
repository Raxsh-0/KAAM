# Ally

App display name: **Ally** (previously KAAM) · applicationId: `com.kaam.app` · internal code keeps the
"Kindred" codename (class names, module packages) — cosmetic only, not user-visible.

Android dating app for open-minded people. Product decisions, feature phases, data model,
and the Play-policy positioning rules live in **[PRODUCT_SPEC.md](PRODUCT_SPEC.md)** — read
that first before changing anything user-facing.

## Status

Phase 0 (scaffold) complete: multi-module Compose app with placeholder screens and
navigation. No backend yet — Firebase arrives in Phase 1.

## Stack

- Kotlin 2.2.21 + Jetpack Compose (Material 3), single activity
- AGP 8.13.2 / Gradle 9.0 — **note:** AGP, Kotlin, Hilt, and several AndroidX pins move
  together. Hilt 2.58+, Kotlin 2.3+, core-ktx 1.18+, lifecycle 2.11+ all require the AGP 9
  generation; migrate them in one step (see comments in `gradle/libs.versions.toml`)
- Hilt for DI (KSP)
- Planned backend: Firebase (Auth, Firestore, Storage, Cloud Functions)

## Modules

| Module | Purpose |
|---|---|
| `app` | Entry point, navigation, bottom bar |
| `core:ui` | Theme + shared composables |
| `core:data` | Data layer; Firebase bindings land here |
| `feature:auth` | Sign-in + age gate (Phase 1) |
| `feature:profile` | Profile, intent + tags (Phase 1) |
| `feature:discovery` | Card stack (Phase 2) |
| `feature:chat` | Matches + messaging (Phase 3) |

## Building

Open in Android Studio (uses its bundled JDK automatically), or from a terminal:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

`local.properties` points at the Android SDK and is machine-specific (gitignored).

## Phase 1 prerequisites (manual, one-time)

1. Create a Firebase project at https://console.firebase.google.com (Blaze plan needed
   for Cloud Functions/Vision later; free quotas cover development).
2. Add an Android app with package `com.kaam.app`, download `google-services.json`
   into `app/` (gitignored).
3. Enable Phone + Google sign-in providers in Firebase Auth.
