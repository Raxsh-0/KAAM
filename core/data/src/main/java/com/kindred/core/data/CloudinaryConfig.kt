package com.kindred.core.data

/**
 * Cloud name and unsigned upload preset are not secrets — Cloudinary's unsigned
 * upload flow is designed to be embedded in client apps. Restrictions (folder,
 * size, formats) are enforced server-side on the preset itself.
 */
object CloudinaryConfig {
    const val CLOUD_NAME = "cchydcyc"
    const val UPLOAD_PRESET = "ally_profiles"

    val isConfigured: Boolean
        get() = CLOUD_NAME != "TODO_CLOUD_NAME" && UPLOAD_PRESET != "TODO_UPLOAD_PRESET"
}
