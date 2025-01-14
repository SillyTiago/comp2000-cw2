package com.example.comp2000_cw2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.CheckBox

// Provides UI and functionality for user-specific settings. In this case, it handles actions such as logging out of the application and enabling/disabling user preferences.
// For the latter, there is still no functionality
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Binds the logout button
        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        val pushNotificationsCheckbox = view.findViewById<CheckBox>(R.id.push_notifications_checkbox)

        val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isPushNotificationsEnabled = sharedPreferences.getBoolean("push_notifications_enabled", false)
        pushNotificationsCheckbox.isChecked = isPushNotificationsEnabled

        pushNotificationsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("push_notifications_enabled", isChecked).apply()
        }

        // Handles logout button functionality
        logoutButton.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
