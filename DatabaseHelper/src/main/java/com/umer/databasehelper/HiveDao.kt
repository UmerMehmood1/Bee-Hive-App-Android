package com.umer.databasehelper

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.umer.databasehelper.entities.Hive

@Dao
interface HiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(hive: Hive)

    @Query("SELECT * FROM hive WHERE beeHiveName = :beeHiveName")
    @RewriteQueriesToDropUnusedColumns
    suspend fun getHiveByName(beeHiveName: String): Hive?

    @Query("SELECT * FROM hive")
    fun getAllHives(): LiveData<List<Hive>>
}
