package com.umer.beehiveclient

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import android.widget.Toast
import com.umer.beehiveclient.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Util {
    fun saveUser(context: Context, user: User){
        fun getCurrentDate(): String {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            return dateFormat.format(Date())
        }
        context.getSharedPreferences("user",Context.MODE_PRIVATE).edit().apply {
            putString("name",user.name)
            putString("email",user.email)
            putString("photoUrl",user.photoUrl)
            putString("uid",user.uid)
            putString("joinedIn",getCurrentDate())
            apply()
        }
    }
    fun clearUser(context: Context){
        context.getSharedPreferences("user",Context.MODE_PRIVATE).edit().apply {
            clear()
            apply()
        }
    }
    fun getUser(context: Context): User? {
        val sharedPreferences = context.getSharedPreferences("user",Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("name","")
        val userEmail = sharedPreferences.getString("email","")
        val userPhotoUrl = sharedPreferences.getString("photoUrl","")
        val userUid = sharedPreferences.getString("uid","")
        val userJoinedIn = sharedPreferences.getString("joinedIn","")
        if (userName!=null && userEmail!=null && userPhotoUrl!=null && userUid!=null && userJoinedIn!=null){
            if (userName.isNotEmpty() && userEmail.isNotEmpty() && userPhotoUrl.isNotEmpty() && userUid.isNotEmpty() && userJoinedIn.isNotEmpty()){
                return User(userName,userEmail,userPhotoUrl,userUid,userJoinedIn)
            }
        }
        return null
    }
    var toast: Toast? = null
    fun showToast(context: Context, message: String){
        toast?.cancel()
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }
    fun saveIsBackedUp(context: Context, isBackedUp: Boolean){
        context.getSharedPreferences("backup",Context.MODE_PRIVATE).edit().apply {
            putBoolean("isBackedUp",isBackedUp)
            apply()
        }
    }
    fun getIsBackedUp(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("backup",Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isBackedUp",false)
    }
    fun saveBackupFrequency(context: Context, frequency: Int){
        context.getSharedPreferences("backup",Context.MODE_PRIVATE).edit().apply {
            putInt("frequency",frequency)
            apply()
        }
    }
    fun getBackupFrequency(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("backup",Context.MODE_PRIVATE)
        return sharedPreferences.getInt("frequency",0)
    }
    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }
}