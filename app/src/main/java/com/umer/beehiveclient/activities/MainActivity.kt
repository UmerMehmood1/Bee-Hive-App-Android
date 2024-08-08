package com.umer.beehiveclient.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.umer.beehiveclient.R
import com.umer.beehiveclient.Util
import com.umer.beehiveclient.adapters.ViewPagerAdapter
import com.umer.beehiveclient.databinding.ActivityMainBinding
import com.umer.beehiveclient.service.DataService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var dataService: DataService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DataService.LocalBinder
            dataService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.viewPager.adapter = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.orientation = androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    binding.viewPager.setCurrentItem(0, true)
                    true
                }
                R.id.alerts -> {
                    binding.viewPager.setCurrentItem(1, true)
                    true
                }
                R.id.settings -> {
                    binding.viewPager.setCurrentItem(2, true)
                    true
                }
                R.id.profile -> {
                    binding.viewPager.setCurrentItem(3, true)
                    true
                }

                else ->{
                    Toast.makeText(this@MainActivity, "In Development Process!!!", Toast.LENGTH_SHORT).show()
                    return@setOnItemSelectedListener false
                }
            }
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            // now, you have permission go ahead
            if (Util.getIsBackedUp(this)){
                Intent(this, DataService::class.java).also { intent ->
                    bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    startService(intent)
                }
            }
        }
        else{
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent(this, DataService::class.java).also { intent ->
                    bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    startService(intent)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Unbind from the service
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

}