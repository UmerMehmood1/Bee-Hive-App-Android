package com.umer.beehiveclient

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class BeeHiveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}