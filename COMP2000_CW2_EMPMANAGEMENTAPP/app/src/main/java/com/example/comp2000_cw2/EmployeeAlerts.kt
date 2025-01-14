package com.example.comp2000_cw2

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class EmployeeAlerts : Fragment(R.layout.fragment_alerts) {

    private val employeeId = 3164

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val alertContainer = view.findViewById<LinearLayout>(R.id.alert_container)
        loadAlerts(alertContainer)
    }

    private fun loadAlerts(container: LinearLayout) {
        val sharedPreferences = requireContext().getSharedPreferences("employee_notifications", Context.MODE_PRIVATE)
        val notificationMessage = sharedPreferences.getString("notification_employee_$employeeId", null)

        if (notificationMessage != null) {
            val alertView = TextView(requireContext()).apply {
                text = notificationMessage
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            container.addView(alertView)

            // Clears the notification after displaying it
            sharedPreferences.edit()
                .remove("notification_employee_$employeeId")
                .apply()
        } else {
            Toast.makeText(requireContext(), "No notifications", Toast.LENGTH_SHORT).show()
        }
    }
}
