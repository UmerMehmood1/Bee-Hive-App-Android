package com.umer.beehiveclient.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.umer.beehiveclient.R
import com.umer.beehiveclient.Util
import com.umer.beehiveclient.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProfileData()
        setListeners()
    }

    private fun setListeners() {
        binding.notificationSwitch.setOnClickListener {
            if (binding.notificationSwitch.isChecked) {
                context?.let {
                    if (ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        binding.notificationSwitch.isChecked = true
                    }
                    else{
                        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                    }
                }
            }
            else{
                binding.notificationSwitch.isChecked = false
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            binding.notificationSwitch.isChecked = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }
    private fun setProfileData() {
        context?.let {
            val user = Util.getUser(context = it)
            binding.profileName.text = user?.name
            binding.profileInfo.text = user?.email
            if (user != null) {
                if (user.photoUrl.isNotEmpty() && user.photoUrl != "null") {
                    Log.d("ProfileFragment", "onViewCreated: ${user.photoUrl}")
                    Glide.with(this).load(user.photoUrl).into(binding.profileImage)
                } else {
                    binding.profileImage.setImageResource(R.drawable.profile_photo)
                }
            } else {
                binding.profileImage.setImageResource(R.drawable.profile_photo)
            }
        }
    }


}