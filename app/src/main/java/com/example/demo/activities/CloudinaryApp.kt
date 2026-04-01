package com.example.demo.activities

import android.app.Application
import com.cloudinary.android.MediaManager

class CloudinaryApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "cloud_name" to "dieajhumq",
            "api_key" to "331231574468938",
            "api_secret" to "ZrdKrPbEloNHsRtLMiPtXJg6Tbk"
        )

        MediaManager.init(this, config)
    }
}