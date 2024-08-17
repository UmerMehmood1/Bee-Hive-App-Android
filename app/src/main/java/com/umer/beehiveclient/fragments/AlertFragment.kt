package com.umer.beehiveclient.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.umer.beehiveclient.R
import com.umer.beehiveclient.adapters.AlertAdapter
import com.umer.beehiveclient.databinding.FragmentAlertBinding
import com.umer.beehiveclient.models.Alert

class AlertFragment : Fragment() {
    private lateinit var binding: FragmentAlertBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val alertList = listOf(
            Alert("Temperature is too high", "Hive #1"),
            Alert("Humidity is too high", "Hive #2"),
            Alert("Sound is too low", "Hive #3"),
            Alert("Light level is too low", "Hive #4"),
            Alert("Weight is too high", "Hive #5"),
            Alert("Temperature is too high", "Hive #6"),
        )
        binding.criticalConditionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = AlertAdapter(alertList)
        binding.criticalConditionRecyclerView.adapter = adapter
    }
    private fun setListeners(){

    }
}