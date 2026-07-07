package com.kindred.core.data

/**
 * Gates the in-app admin panel and premium curation screens. This is UX-only — the real
 * enforcement is a matching Firestore security rule checking `request.auth.token.email`
 * against this same list, since anyone can decompile the APK and see this constant.
 * Without the matching rule, this gate is cosmetic and any other user could read/write
 * data by calling Firestore directly.
 *
 * The value comes from admin.properties (gitignored, not committed — this repo is
 * public) via a BuildConfig field. Comma-separated to support more than one curator.
 * See core/data/build.gradle.kts.
 */
object AdminConfig {
    val ADMIN_EMAILS: Set<String> = BuildConfig.ADMIN_EMAILS
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

    fun isAdmin(email: String?): Boolean = email != null && email in ADMIN_EMAILS
}
