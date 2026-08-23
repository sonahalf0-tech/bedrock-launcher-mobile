package com.bedrock.launcher.installer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class LaunchManager(private val context: Context) {

    fun isMinecraftInstalled(packageName: String = "com.mojang.minecraftpe"): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getInstalledVersionName(packageName: String = "com.mojang.minecraftpe"): String? {
        return try {
            val pInfo = context.packageManager.getPackageInfo(packageName, 0)
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun launchMinecraft(packageName: String = "com.mojang.minecraftpe"): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }
}
