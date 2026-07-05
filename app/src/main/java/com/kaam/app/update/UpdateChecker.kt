package com.kaam.app.update

import android.content.Context
import android.os.Build
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class UpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val notes: String,
)

object UpdateChecker {

    // Raw URL of the version manifest. Bump version.json in that repo to push an
    // update prompt to every installed copy of the app.
    private const val MANIFEST_URL =
        "https://raw.githubusercontent.com/Raxsh-0/KAAM/main/version.json"

    fun installedVersionCode(context: Context): Long {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode
        else info.versionCode.toLong()
    }

    /** Returns update info if a newer version is published, null otherwise (or on any failure). */
    suspend fun check(currentVersionCode: Long): UpdateInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(MANIFEST_URL).openConnection() as HttpURLConnection
            connection.connectTimeout = 6000
            connection.readTimeout = 6000
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val info = UpdateInfo(
                versionCode = json.getLong("versionCode"),
                versionName = json.getString("versionName"),
                apkUrl = json.getString("apkUrl"),
                notes = json.optString("notes"),
            )
            info.takeIf { it.versionCode > currentVersionCode }
        }.getOrNull()
    }
}
