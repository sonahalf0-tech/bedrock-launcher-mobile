package com.bedrock.launcher

import android.app.Application
import com.bedrock.launcher.data.local.LauncherDatabase

class BedrockLauncherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Room Database eagerly
        LauncherDatabase.getInstance(this)
    }
}
