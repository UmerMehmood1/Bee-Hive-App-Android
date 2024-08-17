package com.umer.beehiveclient.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.umer.beehiveclient.R
import com.umer.beehiveclient.Util
import com.umer.beehiveclient.activities.HiveDetailsActivity
import com.umer.beehiveclient.databinding.HiveItemBinding
import com.umer.beehiveclient.models.Beehive
import com.umer.databasehelper.HiveRepository
import com.umer.databasehelper.database.HiveDatabase
import com.umer.databasehelper.entities.Hive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class HiveAdapter : ListAdapter<Hive, HiveAdapter.HiveViewHolder>(HiveDiffCallback()) {

    inner class HiveViewHolder(private val binding: HiveItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(hive: Hive, position: Int) {
            binding.hiveName.text = hive.beeHiveName
            binding.beesAmount.text = "${hive.beeCountValue} bees"
            binding.temperatureAmount.text = hive.temperatureValue
            binding.humidityAmount.text = hive.humidityValue
            binding.lightLevelAmount.text = hive.lightLevelValue
            binding.weightAmount.text = "${hive.weightValue} kg"
            binding.soundAmount.text = "${hive.soundValue} dB"
            binding.hiveImage.setImageResource(R.drawable.bees_1)
            checkAndUpdateBeehive(binding, binding.root.context, hive.beeHiveName, position)
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, HiveDetailsActivity::class.java)
                context.startActivity(intent)
            }
        }
    }



    private fun checkAndUpdateBeehive(binding: HiveItemBinding, context: Context, hiveCode: String, position: Int) {
        val databaseOffline = HiveDatabase.getDatabase(context)
        val hiveRepository = HiveRepository(databaseOffline.hiveDao())
        if (hiveCode == "beehive"){
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // The beehive exists, get live data
                        addLiveDataListener(binding,context, databaseReference, hiveRepository, hiveCode, position)
                    } else {
                        // The beehive does not exist
                        Util.showToast(context, "Beehive does not exist. Contact admin for access.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle any errors
                    Log.e("DatabaseError", "Failed to check existence", error.toException())
                    Util.showToast(context, "Failed to check beehive existence")
                }
            })

        }

        // Check if the beehive exists
    }

    private fun addLiveDataListener(
        binding: HiveItemBinding,
        context: Context,
        databaseReference: DatabaseReference,
        hiveRepository: HiveRepository,
        hiveCode: String,
        position: Int
    ) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val beehiveData = snapshot.getValue(Beehive::class.java)
                if (beehiveData != null) {
                    // Convert Beehive data to Hive and update the database
                    CoroutineScope(Dispatchers.IO).launch {
                        hiveRepository.addOrUpdateHive(beehiveData.toHive(hiveCode))
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.hiveName.text = beehiveData.toHive(hiveCode).beeHiveName
                            binding.beesAmount.text = "${beehiveData.toHive(hiveCode).beeCountValue} bees"
                            binding.temperatureAmount.text = beehiveData.toHive(hiveCode).temperatureValue
                            binding.humidityAmount.text = beehiveData.toHive(hiveCode).humidityValue
                            binding.lightLevelAmount.text = beehiveData.toHive(hiveCode).lightLevelValue
                            binding.weightAmount.text = "${beehiveData.toHive(hiveCode).weightValue} kg"
                            binding.soundAmount.text = "${beehiveData.toHive(hiveCode).soundValue} dB"
                        }
                    }
                } else {
                    Log.w("DatabaseWarning", "Beehive data is null")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors
                Log.e("DatabaseError", "Failed to retrieve live data", error.toException())
                Util.showToast(context, "Failed to retrieve live data")
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiveViewHolder {
        val binding = HiveItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HiveViewHolder, position: Int) {
        val hive = getItem(position)
        holder.bind(hive, position)
    }


}

class HiveDiffCallback : DiffUtil.ItemCallback<Hive>() {
    override fun areItemsTheSame(oldItem: Hive, newItem: Hive): Boolean {
        // Assuming `beeHiveName` is unique for each item
        return oldItem.beeHiveName == newItem.beeHiveName
    }

    override fun areContentsTheSame(oldItem: Hive, newItem: Hive): Boolean {
        // Check if the content of the items is the same
        return oldItem == newItem
    }
}

