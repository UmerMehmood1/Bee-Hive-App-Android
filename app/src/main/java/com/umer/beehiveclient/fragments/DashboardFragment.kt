package com.umer.beehiveclient.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.umer.beehiveclient.adapters.HiveAdapter
import com.umer.beehiveclient.bottomSheets.AddHiveBottomSheet
import com.umer.beehiveclient.databinding.FragmentDashboardBinding
import com.umer.databasehelper.HiveRepository
import com.umer.databasehelper.database.HiveDatabase
import com.umer.databasehelper.entities.Hive
import java.util.Locale

class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var adapter: HiveAdapter
    private lateinit var hiveRepository: HiveRepository
    private lateinit var hiveDatabase: HiveDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hiveDatabase = HiveDatabase.getDatabase(requireContext())
        hiveRepository = HiveRepository(hiveDatabase.hiveDao())

        binding.hiveRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HiveAdapter()
        binding.hiveRecyclerView.adapter = adapter

        // Observe LiveData from the repository
        hiveRepository.getAllHives().observe(viewLifecycleOwner, Observer { hives ->
            adapter.submitList(hives)
            filterHives(binding.searchView.text.toString())
        })

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

        binding.addNewHiveButton.setOnClickListener {
            AddHiveBottomSheet(requireContext())
        }
    }

    private fun filterHives(query: String?) {
        val lowerCaseQuery = query?.lowercase(Locale.getDefault()) ?: ""
        val filteredHives = if (lowerCaseQuery.isEmpty()) {
            adapter.currentList
        } else {
            adapter.currentList.filter {
                it.beeHiveName.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.temperatureValue.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.weightValue.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        it.soundValue.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            }
        }
        adapter.submitList(filteredHives)
    }
}
