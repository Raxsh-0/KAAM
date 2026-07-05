package com.kaam.app

import android.app.Application
import com.cloudinary.android.MediaManager
import com.kindred.core.data.CloudinaryConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KindredApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (CloudinaryConfig.isConfigured) {
            MediaManager.init(this, mapOf("cloud_name" to CloudinaryConfig.CLOUD_NAME))
        }
    }
}
