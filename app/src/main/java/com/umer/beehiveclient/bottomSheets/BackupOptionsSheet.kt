package com.umer.beehiveclient.bottomSheets

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.umer.beehiveclient.databinding.BackupOptionBottomSheetBinding
import com.umer.beehiveclient.listeners.BackupOptionListener

class BackupOptionsSheet(context: Context, private val listener: BackupOptionListener): BottomSheetDialog(context) {
    companion object{
        const val HOURLY = 0
        const val DAILY = 1
        const val WEEKLY = 2
    }
    private var binding: BackupOptionBottomSheetBinding = BackupOptionBottomSheetBinding.inflate(layoutInflater)

    init {
        show()
        setContentView(binding.root)
        binding.hourly.setOnClickListener {
            listener.onOptionSelected(HOURLY)
            dismiss()
        }
        binding.daily.setOnClickListener {
            listener.onOptionSelected(DAILY)
            dismiss()
        }
        binding.weekly.setOnClickListener {
            listener.onOptionSelected(WEEKLY)
            dismiss()
        }
    }

}
