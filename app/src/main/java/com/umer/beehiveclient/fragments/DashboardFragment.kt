package com.umer.beehiveclient.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.umer.beehiveclient.adapters.HiveAdapter
import com.umer.beehiveclient.databinding.FragmentDashboardBinding
import com.umer.beehiveclient.models.HiveModel
import java.util.Locale

class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var adapter: HiveAdapter
    private var hiveList = listOf(
        HiveModel("Hive 1", 100, "25°C", "50%", "25dB"),
        HiveModel("Hive 2", 150, "30°C", "60%", "30dB"),
        HiveModel("Hive 3", 200, "35°C", "70%", "35dB")
    )
    private var filteredHiveList = hiveList.toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.hiveRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HiveAdapter(filteredHiveList)
        binding.hiveRecyclerView.adapter = adapter

        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterHives(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })
    }

    private fun filterHives(query: String?) {
        val lowerCaseQuery = query?.lowercase(Locale.getDefault()) ?: ""
        filteredHiveList = if (lowerCaseQuery.isEmpty()) {
            hiveList.toMutableList()
        } else {
            hiveList.filter {
                it.name.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.temperature.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.humidity.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.sound.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            }.toMutableList()
        }
        adapter.updateList(filteredHiveList)
    }
}
