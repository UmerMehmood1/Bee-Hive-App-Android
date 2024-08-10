package com.umer.databasehelper.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.umer.databasehelper.HiveDao
import com.umer.databasehelper.entities.Hive

@Database(entities = [Hive::class], version = 1, exportSchema = false)
abstract class HiveDatabase : RoomDatabase() {
    abstract fun hiveDao(): HiveDao

    companion object {
        @Volatile
        private var INSTANCE: HiveDatabase? = null

        fun getDatabase(context: Context): HiveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HiveDatabase::class.java,
                    "hive_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
