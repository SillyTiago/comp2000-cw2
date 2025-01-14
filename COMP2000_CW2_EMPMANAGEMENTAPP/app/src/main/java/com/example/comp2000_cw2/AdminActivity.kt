package com.example.comp2000_cw2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.comp2000_cw2.databinding.AdminBinding

// Serves as the main page for administrators. It handles navigation between each administrator fragment using BottomNavigationView
class AdminActivity : AppCompatActivity() {

    // Binding object for accessing UI elements in the admin page
    private lateinit var binding: AdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflates the layout and sets it as the content view
        binding = AdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sets the defaults fragment to AdminHome when the Admin Activity is first called (so when administrator first logs in)
        replaceFragment(AdminHome())

        // Handles the selection of items for navigation in BottomNavigationView
        binding.bottomNavigationView3.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.adminhome -> replaceFragment(AdminHome()) // Navigate to Admin Home
                R.id.alerts -> replaceFragment(AdminAlerts()) // Navigate to Alerts
                R.id.settings -> replaceFragment(SettingsFragment()) // Navigate to Settings
                else -> false
            }
            true
        }
    }

    // Function to replace the current fragment with the next provided fragment
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}
