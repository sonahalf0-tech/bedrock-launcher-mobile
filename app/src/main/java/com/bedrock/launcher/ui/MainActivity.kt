package com.bedrock.launcher.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.bedrock.launcher.data.repository.LauncherRepository
import com.bedrock.launcher.ui.navigation.AppNavigation
import com.bedrock.launcher.ui.theme.BackgroundDark
import com.bedrock.launcher.ui.theme.BedrockLauncherTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private lateinit var repository: LauncherRepository

    private val manageStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Handled storage permissions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = LauncherRepository(this)

        checkAndRequestStoragePermissions()
        handleIncomingIntent(intent)

        setContent {
            BedrockLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun checkAndRequestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                manageStoragePermissionLauncher.launch(intent)
            }
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data: Uri? = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            val scheme = data.scheme
            val path = data.path ?: ""

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val tempFile = File(cacheDir, "incoming_${System.currentTimeMillis()}_${data.lastPathSegment}")
                    contentResolver.openInputStream(data)?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (path.endsWith(".apk", ignoreCase = true) || data.toString().endsWith(".apk", ignoreCase = true)) {
                        repository.importApkFile(tempFile)
                    } else if (path.endsWith(".mcpack", ignoreCase = true) ||
                        path.endsWith(".mcaddon", ignoreCase = true) ||
                        path.endsWith(".mcworld", ignoreCase = true)
                    ) {
                        repository.importAddonFile(tempFile)
                    }

                    tempFile.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
