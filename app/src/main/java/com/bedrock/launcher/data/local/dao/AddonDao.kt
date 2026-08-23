package com.bedrock.launcher.data.local.dao

import androidx.room.*
import com.bedrock.launcher.data.local.entity.AddonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddonDao {
    @Query("SELECT * FROM bedrock_addons ORDER BY dateImported DESC")
    fun getAllAddons(): Flow<List<AddonEntity>>

    @Query("SELECT * FROM bedrock_addons WHERE type = :type ORDER BY dateImported DESC")
    fun getAddonsByType(type: String): Flow<List<AddonEntity>>

    @Query("SELECT * FROM bedrock_addons WHERE id = :id LIMIT 1")
    suspend fun getAddonById(id: Long): AddonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddon(addon: AddonEntity): Long

    @Update
    suspend fun updateAddon(addon: AddonEntity)

    @Delete
    suspend fun deleteAddon(addon: AddonEntity)

    @Query("UPDATE bedrock_addons SET isEnabledForDefaultProfile = :enabled WHERE id = :id")
    suspend fun toggleAddonEnabled(id: Long, enabled: Boolean)
}
