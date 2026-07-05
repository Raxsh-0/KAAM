package com.kindred.core.data

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

@Singleton
class PhotoRepository @Inject constructor() {

    /** Uploads to Cloudinary (unsigned preset) and returns the resulting HTTPS image URL. */
    suspend fun uploadPhoto(uri: Uri): String {
        check(CloudinaryConfig.isConfigured) { "Photo storage isn't set up yet." }
        return withTimeout(30_000) {
            suspendCancellableCoroutine { continuation ->
                val requestId = MediaManager.get().upload(uri)
                    .unsigned(CloudinaryConfig.UPLOAD_PRESET)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) = Unit
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) = Unit

                        override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>) {
                            val url = resultData["secure_url"] as? String
                            if (url != null) continuation.resume(url)
                            else continuation.resumeWithException(IllegalStateException("Upload succeeded but no URL returned"))
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            continuation.resumeWithException(IllegalStateException(error.description))
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            continuation.resumeWithException(IllegalStateException(error.description))
                        }
                    })
                    .dispatch()

                continuation.invokeOnCancellation { MediaManager.get().cancelRequest(requestId) }
            }
        }
    }
}
