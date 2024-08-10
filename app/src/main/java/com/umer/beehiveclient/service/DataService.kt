package com.umer.beehiveclient.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.umer.beehiveclient.R
import com.umer.beehiveclient.Util
import com.umer.beehiveclient.activities.MainActivity
import com.umer.beehiveclient.bottomSheets.BackupOptionsSheet.Companion.DAILY
import com.umer.beehiveclient.bottomSheets.BackupOptionsSheet.Companion.HOURLY
import com.umer.beehiveclient.bottomSheets.BackupOptionsSheet.Companion.WEEKLY
import com.umer.beehiveclient.listeners.OnInternetStateChanged
import com.umer.beehiveclient.recievers.NetworkChangeReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DataService : Service() {
    private val channelId = "DataServiceChannel"
    private val notificationId = 1
    private var fileName = ""
    private lateinit var jsonFile: java.io.File
    private lateinit var database: DatabaseReference
    private val binder = LocalBinder()
    private lateinit var networkChangeReceiver : NetworkChangeReceiver
    inner class LocalBinder : Binder() {
        fun getService(): DataService = this@DataService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fileName = dataDir.path + "/sensor_data.json"
        jsonFile = java.io.File(fileName)
        if (!jsonFile.exists()) {
            jsonFile.createNewFile()
        }
        showNotification()

        database = FirebaseDatabase.getInstance("https://beehive-7a398-default-rtdb.firebaseio.com/").reference
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Data Service Channel"
            val descriptionText = "Channel for Data Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val backupOption = Util.getBackupFrequency(this)
        val handler = Handler(Looper.getMainLooper())
        val fetchRunnable = object : Runnable {
            override fun run() {
                fetchDataFromFirebase()
                val hoursInMillis = 60 * 60 * 1000L
                val dayInMillis = 24 * hoursInMillis
                val weekInMillis = 7 * dayInMillis
                // Handle scheduling based on backupOption
                when (backupOption) {
                    HOURLY -> {
                        handler.postDelayed(this, hoursInMillis)
                    }
                    DAILY -> {
                        handler.postDelayed(this, dayInMillis)
                    }
                    WEEKLY -> {
                        handler.postDelayed(this, weekInMillis)
                    }
                    else -> {
                        handler.postDelayed(this, 5000) // Default interval of 5 seconds
                    }
                }
            }
        }

        handler.post(fetchRunnable)
        startForeground(notificationId, getNotification("Fetching data..."))
        return START_STICKY
    }
    private fun showNotification() {
        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = channelId
            val description = "Data Service"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            NotificationCompat.Builder(this, channelId).setSilent(true)
        } else {
            NotificationCompat.Builder(this)
        }
        val notification = notificationBuilder
            .setSmallIcon(R.drawable.bee_icon)
            .setContentTitle("Data Service")
            .setSilent(true)
            .setContentText("Fetching data from Firebase and uploading to Drive")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
    private fun getNotification(contentText: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Data Service")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.bee_icon)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
    private fun updateNotification(contentText: String) {
        val notification = getNotification(contentText)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
    private fun fetchDataFromFirebase() {
        database.child("hiveboxes/beehive").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataMap = snapshot.value as? Map<*, *>
                dataMap?.let {
                    val jsonData = JSONObject(it).toString()
                    updateJsonFile(jsonData)
                    updateNotification("Data fetched and saved")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                updateNotification("Failed to fetch data")
            }
        })
    }
    private fun updateJsonFile(data: String) {
        try {
            // Format timestamp according to API level
            val timestamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.time.ZonedDateTime.now().toString() // API 26+
            } else {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                dateFormat.format(Date()) // For API < 26
            }

            val newData = JSONObject(data).apply {
                put("timestamp", timestamp) // Add timestamp
            }

            // Read existing data
            val existingData = try {
                val fileContent = jsonFile.readText()
                val existingJson = JSONObject(fileContent)
                existingJson.optJSONArray("data") ?: JSONArray()
            } catch (e: Exception) {
                JSONArray()
            }

            // Append new data to existing data
            val updatedData = JSONObject().apply {
                put("data", JSONArray().apply {
                    put(existingData)
                    put(newData)
                })
            }

            jsonFile.writeText(updatedData.toString())

            // Register network change receiver
            networkChangeReceiver = NetworkChangeReceiver(object : OnInternetStateChanged {
                override fun onConnected() {
                    uploadFileToDrive()
                }

                override fun onDisconnected() {
                    updateNotification("Please connect to the internet for history saving feature")
                }
            })
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(networkChangeReceiver, filter)

        } catch (e: IOException) {
            e.printStackTrace()
            updateNotification("Failed to update JSON file")
        }
    }
    private fun uploadFileToDrive() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
                val credential = GoogleAccountCredential.usingOAuth2(
                    applicationContext, listOf(DriveScopes.DRIVE_FILE)
                ).setSelectedAccount(account?.account)
                val driveService = Drive.Builder(
                    NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName(getString(R.string.app_name)).build()

                val result = driveService.files().list()
                    .setQ("name = '${jsonFile.name}' and trashed = false")
                    .execute()

                if (result.files.isNotEmpty()) {
                    val fileId = result.files[0].id
                    val mediaContent = FileContent("application/json", jsonFile)
                    driveService.files().update(fileId, null, mediaContent).execute()
                } else {
                    val fileMetadata = File()
                    fileMetadata.name = fileName
                    val mediaContent = FileContent("application/json", jsonFile)
                    driveService.files().create(fileMetadata, mediaContent).execute()
                }

                withContext(Dispatchers.Main) {
                    updateNotification("File uploaded successfully")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateNotification("Upload failed: ${e.message}")
                }
            }
        }
    }
}
