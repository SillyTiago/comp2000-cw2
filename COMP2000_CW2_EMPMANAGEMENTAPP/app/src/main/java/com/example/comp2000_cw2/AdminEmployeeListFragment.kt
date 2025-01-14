package com.example.comp2000_cw2

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

// Provides the necessary UI and functionality for administrators to see all the existing users in the API server
class AdminEmployeeListFragment : Fragment(R.layout.fragment_employee_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Binds UI elements
        val goBackButton = view.findViewById<Button>(R.id.go_back_button)
        val container = view.findViewById<LinearLayout>(R.id.employee_list_container)

        // Navigates back to the previous screen when the "Go Back" button is pressed
        goBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Fetches and displays the list of all employees
        CoroutineScope(Dispatchers.IO).launch {
            val employeeList = fetchEmployeeList()
            if (employeeList != null) {
                withContext(Dispatchers.Main) {
                    populateEmployeeList(employeeList, container)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to fetch employee list", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function that handles the fetching of the Employee List, from the API
    private fun fetchEmployeeList(): List<JSONObject>? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.224.41.11/comp2000/employees")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body!!.string()
                    val jsonArray = JSONArray(responseBody)
                    val employeeList = mutableListOf<JSONObject>()
                    for (i in 0 until jsonArray.length()) {
                        employeeList.add(jsonArray.getJSONObject(i))
                    }
                    employeeList
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Responsible for populating the Container with Employee Details (for the list)
    private fun populateEmployeeList(employeeList: List<JSONObject>, container: LinearLayout) {
        if (employeeList.isEmpty()) {
            // Show "No employees to display" message if the list is empty
            val noEmployeesText = TextView(requireContext()).apply {
                text = "No employees to display"
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(16, 16, 16, 16)
            }
            container.addView(noEmployeesText)
            return
        }

        for (employee in employeeList) {
            val employeeId = employee.optInt("id", -1) // Default to -1 if not present
            val fullName = "${employee.optString("firstname", "N/A")} ${employee.optString("lastname", "N/A")}"
            val email = employee.optString("email", "N/A")
            val department = employee.optString("department", "N/A")
            val salary = employee.optString("salary", "N/A")
            val joiningDate = employee.optString("joiningdate", "N/A")
            val leaves = employee.optInt("leaves", 0) // Default to 0 if null

            // Dynamically create views for each employee
            val employeeView = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(resources.getColor(android.R.color.white, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
            }

            val employeeDetailsIdText = TextView(requireContext()).apply {
                text = "EmployeeDetails ID: $employeeId"
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, 0, 0, 8)
            }

            val fullNameText = TextView(requireContext()).apply {
                text = fullName
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, 0, 0, 8)
            }

            val emailText = TextView(requireContext()).apply {
                text = email
                textSize = 14f
                setPadding(0, 0, 0, 8)
            }

            val departmentText = TextView(requireContext()).apply {
                text = "Department: $department"
                textSize = 14f
                setPadding(0, 0, 0, 8)
            }

            val salaryText = TextView(requireContext()).apply {
                text = "Salary: $salary"
                textSize = 14f
                setPadding(0, 0, 0, 8)
            }

            val joiningDateText = TextView(requireContext()).apply {
                text = "Joining Date: $joiningDate"
                textSize = 14f
                setPadding(0, 0, 0, 8)
            }

            val leavesText = TextView(requireContext()).apply {
                text = "Leaves: $leaves"
                textSize = 14f
                setPadding(0, 0, 0, 8)
            }

            employeeView.addView(employeeDetailsIdText)
            employeeView.addView(fullNameText)
            employeeView.addView(emailText)
            employeeView.addView(departmentText)
            employeeView.addView(salaryText)
            employeeView.addView(joiningDateText)
            employeeView.addView(leavesText)

            container.addView(employeeView)
        }
    }
}
