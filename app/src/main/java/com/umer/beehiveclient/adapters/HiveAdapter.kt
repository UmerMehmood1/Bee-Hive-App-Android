package com.umer.beehiveclient.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umer.beehiveclient.R
import com.umer.beehiveclient.activities.HiveDetailsActivity
import com.umer.beehiveclient.databinding.HiveItemBinding
import com.umer.beehiveclient.models.HiveModel

class HiveAdapter(private var hiveList: List<HiveModel>) : RecyclerView.Adapter<HiveAdapter.HiveViewHolder>() {

    inner class HiveViewHolder(private val binding: HiveItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hive: HiveModel) {
            binding.hiveName.text = hive.name
            binding.beesAmount.text = "${hive.beesCount} bees"
            binding.temperatureAmount.text = hive.temperature
            binding.humidityAmount.text = hive.humidity
            binding.soundAmount.text = hive.sound
            // Assuming you have a drawable resource ID in HiveModel for the image
            binding.hiveImage.setImageResource(R.drawable.bees_1)
            binding.root.setOnClickListener {
                // Handle item click event here
                binding.root.context.startActivity(Intent(binding.root.context, HiveDetailsActivity::class.java))
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiveViewHolder {
        val binding = HiveItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HiveViewHolder, position: Int) {
        holder.bind(hiveList[position])
    }

    override fun getItemCount(): Int = hiveList.size

    fun updateList(newList: List<HiveModel>) {
        hiveList = newList
        notifyDataSetChanged()
    }
}
