package com.umer.beehiveclient.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        context?.let {
            val user = Util.getUser(context = it)
            binding.profileName.text = user?.name
            binding.profileInfo.text = user?.email
            if (user != null){
                if (user.photoUrl.isNotEmpty() && user.photoUrl != "null"){
                    Log.d("ProfileFragment", "onViewCreated: ${user.photoUrl}")
                    binding.profileImage.setImageURI(Uri.parse(user.photoUrl))
                }
                else{
                    binding.profileImage.setImageResource(R.drawable.profile_photo)
                }
            }
            else{
                binding.profileImage.setImageResource(R.drawable.profile_photo)
            }


        }
    }


}