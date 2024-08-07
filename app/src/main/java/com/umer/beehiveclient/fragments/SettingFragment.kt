package com.umer.beehiveclient.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.umer.beehiveclient.R
import com.umer.beehiveclient.Util
import com.umer.beehiveclient.bottomSheets.BackupOptionsSheet
import com.umer.beehiveclient.databinding.FragmentProfileBinding
import com.umer.beehiveclient.databinding.FragmentSettingBinding
import com.umer.beehiveclient.listeners.BackupOptionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
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
        uploadFile()
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

            binding.backupSwitch.isChecked = binding.backupSwitch.isChecked
        }
    }

    private fun uploadFile() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                context?.let {
                    val account = GoogleSignIn.getLastSignedInAccount(it)
                    account?.account?.let { accountNotNull ->
                        val credential = GoogleAccountCredential.usingOAuth2(
                            it, listOf(Scopes.DRIVE_FILE)
                        ).apply {
                            selectedAccount = accountNotNull
                        }

                        val driveService = Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            JacksonFactory.getDefaultInstance(),
                            credential
                        )
                            .setApplicationName("Your Application Name")
                            .build()

                        // Create a file metadata object
                        val fileMetadata = File()
                        fileMetadata.name = "history.json"
                        fileMetadata.mimeType = "application/json"

                        // Create the JSON data
                        val jsonData = """
                        {
                            "history": [
                                {"event": "File uploaded", "timestamp": "${System.currentTimeMillis()}"}
                            ]
                        }
                    """.trimIndent()

                        val byteArrayOutputStream = ByteArrayOutputStream()
                        byteArrayOutputStream.write(jsonData.toByteArray())

                        val mediaContent = ByteArrayContent(
                            "application/json",
                            byteArrayOutputStream.toByteArray()
                        )

                        // Upload the file to Google Drive
                        val file = driveService.files().create(fileMetadata, mediaContent)
                            .setFields("id")
                            .execute()

                        withContext(Dispatchers.Main) {
                            // Update the UI or notify user of success
                            Toast.makeText(
                                it,
                                "File uploaded successfully: ${file.id}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    context?.let {
                        // Handle errors
                        Toast.makeText(
                            it,
                            "Upload failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

}