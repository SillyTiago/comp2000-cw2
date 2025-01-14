package com.example.comp2000_cw2

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class EmployeeHolidays : Fragment(R.layout.fragment_holidays) {

    private val employeeId = 3164

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val leaveDaysEditText = view.findViewById<EditText>(R.id.leave_days_input)
        val submitButton = view.findViewById<Button>(R.id.submit_button)

        submitButton.setOnClickListener {
            val leaveDays = leaveDaysEditText.text.toString().toIntOrNull()

            if (leaveDays == null || leaveDays <= 0 || leaveDays > 30) {
                Toast.makeText(requireContext(), "Enter a valid number of days (1-30)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveLeaveRequest(employeeId, leaveDays)
            Toast.makeText(requireContext(), "Leave request for $leaveDays days submitted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLeaveRequest(employeeId: Int, leaveDays: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("leave_requests", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("employee_$employeeId", leaveDays)
            .apply()
    }
}


