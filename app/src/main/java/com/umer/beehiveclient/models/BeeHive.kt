package com.umer.beehiveclient.models

import com.umer.databasehelper.entities.Hive

data class Beehive(
    val beeCountValue: String = "",
    val lightValue: String = "",
    val soundValue: String = "",
    val temperatureValue: String = "",
    val weightValue: String = ""
){
    fun toHive(name: String): Hive{
        return Hive(
            beeHiveName = name,
            beeCountValue = beeCountValue,
            lightValue = lightValue,
            soundValue = soundValue,
            temperatureValue = temperatureValue,
            weightValue = weightValue
        )
    }
}