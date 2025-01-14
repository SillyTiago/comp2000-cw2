package com.example.comp2000_cw2

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.widget.Button

// This makes sure to display all details about the employee in the Employee Fragment
class EmployeeDetails : Fragment(R.layout.fragment_employee) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Binds UI elements
        val fullName = view.findViewById<EditText>(R.id.full_name)
        val email = view.findViewById<EditText>(R.id.email)
        val department = view.findViewById<EditText>(R.id.department)
        val salary = view.findViewById<EditText>(R.id.salary)
        val joiningDate = view.findViewById<EditText>(R.id.joiningdate)
        val leaves = view.findViewById<EditText>(R.id.leavedate)

        val editButton = view.findViewById<Button>(R.id.edit_button)
        editButton.setOnClickListener {
            // Navigates to the EditEmployeeFragment once the Edit Details button is clicked
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, EditEmployeeFragment())
                .addToBackStack(null)
                .commit()
        }

        // Fetches and displays employee data
        CoroutineScope(Dispatchers.IO).launch {
            val response = fetchEmployeeData(3164)
            if (response != null) {
                withContext(Dispatchers.Main) {
                    // Populates fields
                    fullName.setText("${response.getString("firstname")} ${response.getString("lastname")}")
                    email.setText(response.getString("email"))
                    department.setText(response.getString("department"))
                    salary.setText(response.getString("salary"))
                    joiningDate.setText(response.getString("joiningdate"))
                    leaves.setText(response.getInt("leaves").toString())

                    // Disables editing for all fields
                    listOf(fullName, email, department, salary, joiningDate, leaves).forEach {
                        it.isEnabled = false
                    }
                }
            }
        }
    }

    // Responsible for the fetching action shown above.
    private fun fetchEmployeeData(employeeId: Int): JSONObject? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.224.41.11/comp2000/employees/get/$employeeId")
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return JSONObject(response.body!!.string())
            }
        }
        return null
    }
}
