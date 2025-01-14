package com.example.comp2000_cw2

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

// This is used to display all Admin Alerts in the admin side of the app (In this case, leave requests submitted by employees)
class AdminAlerts : Fragment(R.layout.fragment_adminalerts) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val alertContainer = view.findViewById<LinearLayout>(R.id.alert_container)
        loadAlerts(alertContainer)
    }

    private fun loadAlerts(container: LinearLayout) {
        val sharedPreferences = requireContext().getSharedPreferences("leave_requests", Context.MODE_PRIVATE)
        val allRequests = sharedPreferences.all

        for ((key, value) in allRequests) {
            val employeeId = key.removePrefix("employee_").toIntOrNull() ?: continue
            val leaveDays = value as? Int ?: continue

            val alertView = TextView(requireContext()).apply {
                text = "Employee $employeeId requested $leaveDays leave days"
                textSize = 16f
                setPadding(16, 16, 16, 16)
                setOnClickListener { navigateToRequestFragment(employeeId, leaveDays) }
            }
            container.addView(alertView)
        }

        if (allRequests.isEmpty()) {
            Toast.makeText(requireContext(), "No leave requests found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToRequestFragment(employeeId: Int, leaveDays: Int) {
        val bundle = Bundle().apply {
            putInt("EMPLOYEE_ID", employeeId)
            putInt("LEAVE_DAYS", leaveDays)
        }

        val fragment = AdminRequestFragment()
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.commit {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
        }
    }
}
