package com.umer.databasehelper

import androidx.lifecycle.LiveData
import com.umer.databasehelper.entities.Hive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HiveRepository(private val hiveDao: HiveDao) {

    // Function to add or update a Hive record
    suspend fun addOrUpdateHive(hive: Hive) {
        withContext(Dispatchers.IO) {
            hiveDao.upsert(hive)
        }
    }

    // Function to get a Hive by its name
    suspend fun getHiveByName(beeHiveName: String): Hive? {
        return withContext(Dispatchers.IO) {
            hiveDao.getHiveByName(beeHiveName)
        }
    }
    fun getAllHives(): LiveData<List<Hive>> {
        return hiveDao.getAllHives()
    }
}
