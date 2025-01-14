package com.example.comp2000_cw2

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class AdminRequestFragment : Fragment(R.layout.fragment_admin_request) {

    private var employeeId: Int = -1
    private var leaveDays: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        employeeId = arguments?.getInt("EMPLOYEE_ID") ?: -1
        leaveDays = arguments?.getInt("LEAVE_DAYS") ?: -1

        val requestDetailsText = view.findViewById<TextView>(R.id.request_details)
        val approveButton = view.findViewById<Button>(R.id.approve_button)
        val denyButton = view.findViewById<Button>(R.id.deny_button)

        requestDetailsText.text = "Employee $employeeId requested $leaveDays days of leave."

        approveButton.setOnClickListener { handleRequest(approve = true) }
        denyButton.setOnClickListener { handleRequest(approve = false) }
    }

    private fun handleRequest(approve: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = if (approve) approveLeaveRequest(employeeId, leaveDays) else denyLeaveRequest(employeeId)

            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(requireContext(), if (approve) "Leave approved" else "Leave denied", Toast.LENGTH_SHORT).show()
                    notifyEmployee(approve)
                    clearRequest(employeeId)
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to process request", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun approveLeaveRequest(employeeId: Int, leaveDays: Int): Boolean {
        val client = OkHttpClient()
        val employeeData = fetchEmployeeData(employeeId) ?: return false

        val originalJoiningDate = employeeData.getString("joiningdate")
        val formattedJoiningDate = convertToIsoDate(originalJoiningDate)

        employeeData.put("leaves", leaveDays)
        employeeData.put("joiningdate", formattedJoiningDate)

        val requestBody = employeeData.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://10.224.41.11/comp2000/employees/edit/$employeeId")
            .put(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response -> response.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun denyLeaveRequest(employeeId: Int): Boolean {
        return true
    }

    private fun fetchEmployeeData(employeeId: Int): JSONObject? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.224.41.11/comp2000/employees/get/$employeeId")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) JSONObject(response.body!!.string()) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun notifyEmployee(approved: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("employee_notifications", Context.MODE_PRIVATE)
        val message = if (approved) "Your leave request for $leaveDays days has been approved." else "Your leave request has been denied."
        sharedPreferences.edit()
            .putString("notification_employee_$employeeId", message)
            .apply()
    }

    private fun clearRequest(employeeId: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("leave_requests", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .remove("employee_$employeeId")
            .apply()
    }

    private fun convertToIsoDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
