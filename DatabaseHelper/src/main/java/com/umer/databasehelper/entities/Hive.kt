package com.umer.databasehelper.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hive")
data class Hive(
    @PrimaryKey val beeHiveName: String,
    val beeCountValue: Int,
    val temperatureValue: String,
    val humidityValue: String,
    val lightLevelValue: String,
    val weightValue: String,
    val soundValue: String
)
