package com.kindred.core.data

/**
 * Gates the in-app admin panel. This is UX-only — the real enforcement is the matching
 * Firestore security rule (`request.auth.token.email == ADMIN_EMAIL`), since anyone can
 * decompile the APK and see this constant. Without the matching rule, this gate is
 * cosmetic and any other user could read/delete profiles by calling Firestore directly.
 *
 * The value itself comes from admin.properties (gitignored, not committed — this repo
 * is public) via a BuildConfig field. See core/data/build.gradle.kts.
 */
object AdminConfig {
    val ADMIN_EMAIL: String = BuildConfig.ADMIN_EMAIL
}
