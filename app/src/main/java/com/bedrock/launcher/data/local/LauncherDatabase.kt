package com.bedrock.launcher.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bedrock.launcher.data.local.dao.AddonDao
import com.bedrock.launcher.data.local.dao.ProfileDao
import com.bedrock.launcher.data.local.dao.VersionDao
import com.bedrock.launcher.data.local.entity.AddonEntity
import com.bedrock.launcher.data.local.entity.ProfileEntity
import com.bedrock.launcher.data.local.entity.VersionEntity

@Database(
    entities = [
        VersionEntity::class,
        ProfileEntity::class,
        AddonEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun versionDao(): VersionDao
    abstract fun profileDao(): ProfileDao
    abstract fun addonDao(): AddonDao

    companion object {
        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getInstance(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "bedrock_launcher.db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
