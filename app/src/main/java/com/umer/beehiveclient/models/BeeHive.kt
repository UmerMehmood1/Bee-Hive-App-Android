package com.umer.beehiveclient.models

import com.umer.databasehelper.entities.Hive

data class Beehive(
    val DHT11: DHT11Data? = null,
    val IR_Sensors: IRSensorsData? = null,
    val LDR: LDRData? = null,
    val Load_Cell: LoadCellData? = null,
    val Sound_Sensor: SoundSensorData? = null
) {
    data class DHT11Data(
        val Humidity: Int? = null,
        val Temperature: Int? = null
    )

    data class IRSensorsData(
        val Count: Int? = null
    )

    data class LDRData(
        val LightLevel: Int? = null
    )

    data class LoadCellData(
        val Weight: Int? = null
    )

    data class SoundSensorData(
        val SoundLevel: Int? = null
    )

    fun toHive(hiveCode: String): Hive {
        val temperatureValue = DHT11?.Temperature?.toString() ?: "0"
        val humidityValue = DHT11?.Humidity?.toString() ?: "0"
        val lightLevelValue = LDR?.LightLevel?.toString() ?: "0"
        val weightValue = Load_Cell?.Weight?.toString() ?: "0"
        val soundValue = Sound_Sensor?.SoundLevel?.toString() ?: "0"
        val beeCountValue = IR_Sensors?.Count ?: 0

        return Hive(
            beeHiveName = hiveCode,
            temperatureValue = "$temperatureValue°C",
            humidityValue = "$humidityValue%",
            lightLevelValue = "$lightLevelValue",
            weightValue = "$weightValue",
            soundValue = "$soundValue",
            beeCountValue = beeCountValue
        )
    }
}
