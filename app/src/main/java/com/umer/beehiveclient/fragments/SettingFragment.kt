package com.umer.beehiveclient.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.utils.Utils
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.umer.beehiveclient.R
import com.umer.beehiveclient.Util
import com.umer.beehiveclient.bottomSheets.BackupOptionsSheet
import com.umer.beehiveclient.bottomSheets.BackupOptionsSheet.Companion.DAILY
import com.umer.beehiveclient.bottomSheets.BackupOptionsSheet.Companion.HOURLY
import com.umer.beehiveclient.bottomSheets.BackupOptionsSheet.Companion.WEEKLY
import com.umer.beehiveclient.bottomSheets.PermissionBottomSheet
import com.umer.beehiveclient.databinding.FragmentProfileBinding
import com.umer.beehiveclient.databinding.FragmentSettingBinding
import com.umer.beehiveclient.listeners.BackupOptionListener
import com.umer.beehiveclient.listeners.OnInternetStateChanged
import com.umer.beehiveclient.listeners.PermissionListener
import com.umer.beehiveclient.recievers.NetworkChangeReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private var permissionBottomSheet: PermissionBottomSheet? = null
    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private var isInternetConnected: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when {
            Util.getBackupFrequency(requireContext()) == 0 -> {
                binding.backupFrequencyText.text = "Hourly"
            }

            Util.getBackupFrequency(requireContext()) == 1 -> {
                binding.backupFrequencyText.text = "Daily"
            }

            Util.getBackupFrequency(requireContext()) == 2 -> {
                binding.backupFrequencyText.text = "Weekly"
            }
        }
        binding.backupSwitch.isChecked = Util.getIsBackedUp(requireContext())
        binding.backupSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked){
                binding.backupNowProgress.visibility = GONE
                binding.backupNowButton.visibility = GONE
            }
            else{
                binding.backupNowButton.visibility = VISIBLE
            }

        }
        if (Util.getIsBackedUp(requireContext())) {
            binding.backupNowProgress.visibility = VISIBLE
        }
        else{
            binding.backupNowProgress.visibility = GONE
        }
        networkChangeReceiver = NetworkChangeReceiver(object : OnInternetStateChanged {
            override fun onConnected() {
                isInternetConnected = true
            }

            override fun onDisconnected() {
                isInternetConnected = false
                Snackbar.make(
                    binding.root,
                    "Please connect to the internet for history saving feature",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context?.registerReceiver(networkChangeReceiver, filter)
        context?.let {
            permissionBottomSheet = PermissionBottomSheet(
                it,
                false,
                "Allow Storage permission for the app to work properly",
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                object : PermissionListener {
                    override fun onSettingClicked() {
                        context?.let {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", context?.packageName, null)
                            intent.setData(uri)
                            startActivityForResult(intent, 101)
                        }
                    }

                }
            )
        }
        setListeners()
    }

    private fun setListeners() {
        binding.frequencyButton.setOnClickListener {
            BackupOptionsSheet(requireContext(), object : BackupOptionListener {
                override fun onOptionSelected(option: Int) {
                    val selectedItem = when (option) {
                        BackupOptionsSheet.HOURLY -> "Hourly"
                        BackupOptionsSheet.DAILY -> "Daily"
                        BackupOptionsSheet.WEEKLY -> "Weekly"
                        else -> "Unknown"
                    }
                    Util.saveBackupFrequency(requireContext(), option)
                    binding.backupFrequencyText.text = selectedItem
                }
            })
        }
        binding.backupSwitch.setOnClickListener {
            context?.let { context ->
                if (binding.backupSwitch.isChecked) {
                    // Handle the case when the switch is turned on
                    Util.saveIsBackedUp(context, true)
                    binding.backupSwitch.isChecked = false
                } else {
                    // Handle the case when the switch is turned off
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        uploadFileToDrive(false)
                    } else {
                        requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            101
                        )
                    }
                }
            }
        }
        binding.backupNowButton.setOnClickListener {
            if (isInternetConnected) {
                uploadFileToDrive(true)
            } else {
                context?.let {
                    Util.showToast(it, "Please connect to the internet")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            context?.let {
                if (it.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    uploadFileToDrive(false)
                } else {
                    Util.showToast(it, "Storage permission not granted")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        permissionBottomSheet?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(networkChangeReceiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // now, you have permission go ahead
                uploadFileToDrive(false)
            } else {
                activity?.let { activity ->
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        // now, user has denied permission (but not permanently!)
                        Util.showToast(
                            it,
                            "This app requires storage permission for backup"
                        )
                    } else {
                        // now, user has denied permission permanently!
                        if (permissionBottomSheet?.isShowing == false) {
                            permissionBottomSheet?.show()
                        }
                    }
                }
            }
        }
        return
    }

    private fun uploadFileToDrive(isFromBackUpButton: Boolean) {
        context?.let {
            val fileName = it.dataDir.path + "/sensor_data.json"
            val jsonFile = java.io.File(fileName)
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val handler = Handler(Looper.getMainLooper())
                    val fetchRunnable = object : java.lang.Runnable {
                        override fun run() {
                            binding.backupNowProgress.visibility = VISIBLE
                            binding.backupNowProgress.progress += binding.backupNowProgress.progress
                            handler.postDelayed(this, 1000)
                        }
                    }
                    handler.post(fetchRunnable)
                    val account = GoogleSignIn.getLastSignedInAccount(it)
                    val credential =
                        GoogleAccountCredential.usingOAuth2(it, listOf(DriveScopes.DRIVE_FILE))
                            .setSelectedAccount(account?.account)
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
                        if (isFromBackUpButton) {
                            binding.backupNowProgress.progress = 100
                            binding.backupNowProgress.visibility = GONE
                            Util.showToast(it, "Backed Up")
                        } else {
                            Util.showToast(it, "Google Drive Connected")
                            binding.backupSwitch.isChecked = true
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isFromBackUpButton) {
                            binding.backupNowProgress.progress = 0
                            binding.backupNowProgress.visibility = GONE
                            Util.showToast(it, "Upload failed...")
                        } else {
                            Util.showToast(it, "Google Drive not connected")
                            binding.backupSwitch.isChecked = false
                        }
                    }
                }
            }
        }
    }


}