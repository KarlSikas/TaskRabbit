package com.example.taskrabbit.db.dao // Adjust package if needed

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taskrabbit.db.entity.CalendarDateEntity // Import the entity
import org.threeten.bp.LocalDate // <<< ADDED: Import LocalDate

@Dao // Tells Room this is a Data Access Object interface
interface CalendarDateDao {

    /**
     * Gets all stored dates as LiveData<LocalDate>, ordered chronologically.
     * Room will automatically convert the stored Long (dateEpochDay) to LocalDate
     * using the TypeConverter defined in your AppDatabase.
     * The LiveData will automatically update the observer when the data changes.
     */
    // --- MODIFIED FUNCTION ---
    @Query("SELECT dateEpochDay FROM calendar_dates ORDER BY dateEpochDay ASC") // <<< CHANGED: Select the Long column directly
    fun getAllDates(): LiveData<List<LocalDate>> // <<< CHANGED: Return type is now LiveData<List<LocalDate>>

    /**
     * Inserts a list of dates into the database.
     * If a date (based on its primary key 'date_epoch_day') already exists,
     * it will be ignored (not replaced).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDates(dates: List<CalendarDateEntity>) // Use suspend for coroutines

    /**
     * Deletes all dates from the table that are older than the specified minimum epoch day.
     */
    @Query("DELETE FROM calendar_dates WHERE date_epoch_day < :minEpochDay")
    suspend fun deleteOlderThan(minEpochDay: Long) // Use suspend for coroutines

    // --- Optional but potentially useful ---
    // /**
    //  * Checks if a specific date exists in the database. Returns 1 if exists, 0 otherwise.
    //  */
    // @Query("SELECT COUNT(*) FROM calendar_dates WHERE date_epoch_day = :epochDay")
    // suspend fun dateExists(epochDay: Long): Int
    //
    // /**
    //  * Deletes dates outside a given range (inclusive). Requires two parameters.
    //  */
    // @Query("DELETE FROM calendar_dates WHERE date_epoch_day < :minEpochDay OR date_epoch_day > :maxEpochDay")
    // suspend fun deleteOutsideRange(minEpochDay: Long, maxEpochDay: Long)

}