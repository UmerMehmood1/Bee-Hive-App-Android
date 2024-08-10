package com.umer.databasehelper.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "hive_table")
data class Hive(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "beeHiveName")
    val beeHiveName: String,

    @ColumnInfo(name = "beeCountValue")
    val beeCountValue: String,

    @ColumnInfo(name = "lightValue")
    val lightValue: String,

    @ColumnInfo(name = "soundValue")
    val soundValue: String,

    @ColumnInfo(name = "temperatureValue")
    val temperatureValue: String,

    @ColumnInfo(name = "weightValue")
    val weightValue: String
)
