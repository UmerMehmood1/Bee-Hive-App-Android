package com.umer.beehiveclient.recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.umer.beehiveclient.listeners.OnInternetStateChanged

class NetworkChangeReceiver(private val onInternetStateChanged: OnInternetStateChanged) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val connectivityManager = it.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            handleConnectionChange(isConnected)
        }
    }

    private fun handleConnectionChange(isConnected: Boolean) {
        if (isConnected) {
            onInternetStateChanged.onConnected()
        } else {
            onInternetStateChanged.onDisconnected()
        }
    }
}
