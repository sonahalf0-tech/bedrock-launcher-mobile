package com.bedrock.launcher.data.local.dao

import androidx.room.*
import com.bedrock.launcher.data.local.entity.VersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VersionDao {
    @Query("SELECT * FROM bedrock_versions ORDER BY versionCode DESC")
    fun getAllVersions(): Flow<List<VersionEntity>>

    @Query("SELECT * FROM bedrock_versions WHERE id = :id LIMIT 1")
    suspend fun getVersionById(id: Long): VersionEntity?

    @Query("SELECT * FROM bedrock_versions WHERE isCurrentlyActive = 1 LIMIT 1")
    fun getActiveVersion(): Flow<VersionEntity?>

    @Query("SELECT * FROM bedrock_versions WHERE isCurrentlyActive = 1 LIMIT 1")
    suspend fun getActiveVersionSync(): VersionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: VersionEntity): Long

    @Update
    suspend fun updateVersion(version: VersionEntity)

    @Delete
    suspend fun deleteVersion(version: VersionEntity)

    @Query("UPDATE bedrock_versions SET isCurrentlyActive = 0")
    suspend fun clearActiveFlags()

    @Transaction
    suspend fun setActiveVersion(id: Long) {
        clearActiveFlags()
        setActiveVersionInternal(id)
    }

    @Query("UPDATE bedrock_versions SET isCurrentlyActive = 1 WHERE id = :id")
    suspend fun setActiveVersionInternal(id: Long)
}
