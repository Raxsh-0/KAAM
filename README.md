# Ally

App display name: **Ally** · applicationId: `com.ally.app`. Internal code still uses the
"Kindred" codename in places (class names, module packages) — cosmetic only, not user-visible.

Android dating app for open-minded people. Product decisions, feature phases, data model,
and the Play-policy positioning rules live in **[PRODUCT_SPEC.md](PRODUCT_SPEC.md)** — read
that first before changing anything user-facing.

## Status

Real Google + email/password sign-in, Firestore-backed profiles, Cloudinary photo upload,
and a mock swipe/match/chat experience are all working. Published via GitHub Releases with
an in-app update prompt (see `version.json` at the repo root).

## Stack

- Kotlin 2.2.21 + Jetpack Compose (Material 3), single activity
- AGP 8.13.2 / Gradle 9.0 — **note:** AGP, Kotlin, Hilt, and several AndroidX pins move
  together. Hilt 2.58+, Kotlin 2.3+, core-ktx 1.18+, lifecycle 2.11+ all require the AGP 9
  generation; migrate them in one step (see comments in `gradle/libs.versions.toml`)
- Hilt for DI (KSP)
- Backend: Firebase (Auth, Firestore) on the free Spark plan
- Photos: Cloudinary (free tier, unsigned upload preset) — Firebase Storage now requires
  the paid Blaze plan for new projects, so it's not used

## Modules

| Module | Purpose |
|---|---|
| `app` | Entry point, navigation, bottom bar, update prompt |
| `core:ui` | Theme + shared composables |
| `core:data` | Auth/Profile/Photo repositories, Firebase + Cloudinary bindings |
| `feature:auth` | Sign-in (Google + email/password) + 18+ age gate |
| `feature:profile` | Profile editing, photo upload, intent + tags |
| `feature:discovery` | Swipe deck (mock data) |
| `feature:chat` | Matches + messaging (mock data) |

## Building

Open in Android Studio (uses its bundled JDK automatically), or from a terminal:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

`local.properties` points at the Android SDK and is machine-specific (gitignored).

## One-time setup (already done for this project, documented for reference)

1. Firebase project at https://console.firebase.google.com, free Spark plan.
2. Android app registered with package `com.ally.app`; `google-services.json` placed in
   `app/` (gitignored). Both the debug and release SHA-1 fingerprints must be added in
   Project settings for Google sign-in to work in both build types.
3. Authentication → enable Google and Email/Password sign-in methods.
4. Firestore Database created (Native mode) with rules restricting each user to their own
   `users/{uid}` document.
5. Cloudinary account, unsigned upload preset — see `CloudinaryConfig.kt` for where the
   cloud name and preset name go (not secrets, safe to commit).

## Releasing an update

1. Bump `versionCode`/`versionName` in `app/build.gradle.kts`.
2. `.\gradlew.bat assembleRelease`
3. Copy the APK into `releases/Ally-x.y.z.apk`.
4. Update `version.json` at the repo root (versionCode, versionName, apkUrl, notes).
5. Commit and push. Installed apps prompt to update on next launch.
