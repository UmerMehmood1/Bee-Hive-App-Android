package com.umer.beehiveclient.bottomSheets

import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.umer.beehiveclient.Util
import com.umer.beehiveclient.databinding.AddHiveBottomSheetBinding
import com.umer.beehiveclient.models.Beehive
import com.umer.databasehelper.HiveRepository
import com.umer.databasehelper.database.HiveDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddHiveBottomSheet(context: Context): BottomSheetDialog(context) {
    private var binding: AddHiveBottomSheetBinding = AddHiveBottomSheetBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        show()
        setListeners()
    }
    private fun setListeners(){
        binding.addHiveButton.setOnClickListener {
            checkAndUpdateBeehive(context, binding.NewHiveCode.text.toString())
        }
    }

    private fun checkAndUpdateBeehive(context: Context, hiveCode: String) {
        val databaseOffline = HiveDatabase.getDatabase(context)
        val hiveRepository = HiveRepository(databaseOffline.hiveDao())

        val databaseReference = FirebaseDatabase.getInstance().reference

        // Check if the beehive exists
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // The beehive exists, get live data
                    addLiveDataListener(databaseReference, hiveRepository, hiveCode)
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

    private fun addLiveDataListener(databaseReference: DatabaseReference, hiveRepository: HiveRepository, hiveCode: String) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val beehiveData = snapshot.getValue(Beehive::class.java)
                if (beehiveData != null) {
                    // Convert Beehive data to Hive and update the database
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            hiveRepository.addOrUpdateHive(beehiveData.toHive(hiveCode))
                            withContext(Dispatchers.Main){
                                Util.showToast(context, "Beehive data added")
                                dismiss()
                            }
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
}