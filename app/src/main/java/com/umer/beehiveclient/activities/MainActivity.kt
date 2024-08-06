package com.umer.beehiveclient.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.umer.beehiveclient.R
import com.umer.beehiveclient.adapters.ViewPagerAdapter
import com.umer.beehiveclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
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

    }
}