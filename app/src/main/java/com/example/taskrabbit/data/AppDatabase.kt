package com.example.taskrabbit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Keep your existing entity imports
import com.example.taskrabbit.data.TaskItem
import com.example.taskrabbit.data.BackgroundImage
// No need for specific converter imports here if using @TypeConverters

@Database(
    entities = [TaskItem::class, BackgroundImage::class],
    // <<< INCREMENT VERSION TO 4 >>>
    version = 4,
    exportSchema = false
)
@TypeConverters(
    // <<< USE THE NEW CONSOLIDATED CONVERTERS CLASS >>>
    Converters::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun backgroundDao(): BackgroundDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        // --- Existing Migration ---
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN reminderMinutesBefore INTEGER DEFAULT NULL")
            }
        }

        // --- NEW MIGRATION FROM VERSION 3 to 4 ---
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new taskTime column to the tasks table
                // Use INTEGER because Converters.kt maps LocalTime? to Int? (seconds of day)
                database.execSQL("ALTER TABLE tasks ADD COLUMN taskTime INTEGER DEFAULT NULL")
            }
        }
        // --- END NEW MIGRATION ---


        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Your database name
                )
                    // --- ADD BOTH MIGRATIONS ---
                    // Room will apply them in order if needed
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4) // <<< Add MIGRATION_3_4
                    // Keep fallbackToDestructiveMigration during development if needed,
                    // but ideally rely on migrations. REMOVE for production.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}