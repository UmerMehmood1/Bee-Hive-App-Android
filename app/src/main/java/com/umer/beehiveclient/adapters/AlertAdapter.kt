package com.umer.beehiveclient.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umer.beehiveclient.R
import com.umer.beehiveclient.databinding.CriticalConditionItemBinding
import com.umer.beehiveclient.models.Alert

class AlertAdapter(private val alertList: List<Alert>) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(private val binding: CriticalConditionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alert: Alert) {
            binding.alertTitle.text = alert.title
            binding.hiveName.text = alert.hiveName
            binding.hiveImage.setImageResource(R.drawable.round_warning_amber_24)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = CriticalConditionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alertList[position]
        holder.bind(alert)
    }

    override fun getItemCount(): Int {
        return alertList.size
    }
}
