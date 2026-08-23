package com.bedrock.launcher.data.local.dao

import androidx.room.*
import com.bedrock.launcher.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM game_profiles ORDER BY lastPlayedTime DESC, id ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM game_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): ProfileEntity?

    @Query("SELECT * FROM game_profiles WHERE isDefault = 1 LIMIT 1")
    fun getActiveProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM game_profiles WHERE isDefault = 1 LIMIT 1")
    suspend fun getActiveProfileSync(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("UPDATE game_profiles SET isDefault = 0")
    suspend fun clearDefaultFlags()

    @Transaction
    suspend fun setDefaultProfile(id: Long) {
        clearDefaultFlags()
        setDefaultProfileInternal(id)
    }

    @Query("UPDATE game_profiles SET isDefault = 1, lastPlayedTime = :time WHERE id = :id")
    suspend fun setDefaultProfileInternal(id: Long, time: Long = System.currentTimeMillis())
}
