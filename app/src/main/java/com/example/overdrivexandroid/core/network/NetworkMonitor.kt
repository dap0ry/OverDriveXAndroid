package com.example.overdrivexandroid.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkMonitor {

    /**
     * Returns true if the device currently has an active connection with
     * INTERNET capability (WiFi, cellular, Ethernet, ...).
     *
     * Requires android.permission.ACCESS_NETWORK_STATE in the Manifest.
     */
    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            // Fallback: assume online so the app doesn't block itself unexpectedly
            true
        }
    }
}
