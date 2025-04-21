package com.example.taskrabbit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper // Added import

import com.example.taskrabbit.data.TaskItem
import com.example.taskrabbit.data.BackgroundImage

@Database(
    entities = [TaskItem::class, BackgroundImage::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(
    Converters::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun backgroundDao(): BackgroundDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN reminderMinutesBefore INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN taskTime INTEGER DEFAULT NULL")
            }
        }

        // Modified function to accept the factory
        fun getDatabase(
            context: Context,
            factory: SupportSQLiteOpenHelper.Factory? = null // Added factory parameter
        ): AppDatabase {
            return Instance ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration() // Consider removing for production

                // Apply the factory if it's provided (for encryption)
                if (factory != null) {
                    builder.openHelperFactory(factory)
                }

                builder.build().also { Instance = it }
            }
        }
    }
}