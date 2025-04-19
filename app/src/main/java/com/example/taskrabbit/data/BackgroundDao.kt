package com.example.taskrabbit.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BackgroundDao {
    @Query("SELECT * FROM backgrounds")
    fun getAllBackgrounds(): Flow<List<BackgroundImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackground(background: BackgroundImage)

    @Update
    suspend fun updateBackground(background: BackgroundImage)

    @Delete
    suspend fun deleteBackground(background: BackgroundImage)

    @Query("SELECT * FROM backgrounds WHERE id = :backgroundId")
    suspend fun getBackgroundById(backgroundId: Long): BackgroundImage?

    @Query("SELECT * FROM backgrounds WHERE isAsset = 1")
    fun getAssetBackgrounds(): Flow<List<BackgroundImage>>

    @Query("SELECT * FROM backgrounds WHERE isPremium = 1")
    fun getPremiumBackgrounds(): Flow<List<BackgroundImage>>

    @Query("SELECT * FROM backgrounds WHERE isAsset = 0 AND isPremium = 0")
    fun getUserBackgrounds(): Flow<List<BackgroundImage>>
}