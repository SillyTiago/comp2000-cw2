package com.example.comp2000_cw2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import java.util.Locale
import java.text.SimpleDateFormat
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// Provides the necessary UI and functionality for administrators to edit an existing employee in the API
class AdminEditEmployeeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflates the fragment layout for editting an existing employee
        return inflater.inflate(R.layout.fragment_admin_edit_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind UI elements.
        val employeeIdEditText = view.findViewById<EditText>(R.id.employee_id)
        val fullNameEditText = view.findViewById<EditText>(R.id.full_name)
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val departmentEditText = view.findViewById<EditText>(R.id.department)
        val salaryEditText = view.findViewById<EditText>(R.id.salary)
        val joiningDateEditText = view.findViewById<EditText>(R.id.joining_date)
        val submitButton = view.findViewById<Button>(R.id.submit_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        // Handles the functionality of the submit button - So once its clicked, it edits the details of the existing Employee from the Database.
        submitButton.setOnClickListener {
            val employeeId = employeeIdEditText.text.toString()
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val department = departmentEditText.text.toString()
            val salary = salaryEditText.text.toString().toFloatOrNull()
            val joiningDate = joiningDateEditText.text.toString()

            // Error Handling
            val empId = employeeId.toIntOrNull()
            if (empId == null || empId <= 0) {
                Toast.makeText(requireContext(), "Valid Employee ID is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fullName.isBlank()) {
                Toast.makeText(requireContext(), "Full Name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Valid Email is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (department.isBlank()) {
                Toast.makeText(requireContext(), "Department is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (salary == null || salary <= 0) {
                Toast.makeText(requireContext(), "Valid Salary is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (joiningDate.isBlank() || !joiningDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                Toast.makeText(requireContext(), "Joining Date must be in YYYY-MM-DD format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calls the method to edit the Employee
            editEmployee(employeeId, fullName, email, department, salary, joiningDate)
        }

        // Handles Cancel button so once its clicked, it goes back to the previous fragment
        cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    // Sends a request to the API server to edit the specified Employee.
    private fun editEmployee(
        id: String,
        fullName: String,
        email: String,
        department: String,
        salary: Float?,
        joiningDate: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingData = fetchEmployeeData(id)
                if (existingData == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Employee not found", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Prepares the updated data payload
                val (firstname, lastname) = fullName.split(" ", limit = 2).let {
                    Pair(it.getOrElse(0) { existingData["firstname"] ?: "" },
                        it.getOrElse(1) { existingData["lastname"] ?: "" })
                }

                val json = JSONObject().apply {
                    put("firstname", firstname)
                    put("lastname", lastname)
                    put("email", email.ifBlank { existingData["email"] ?: "" })
                    put("department", department.ifBlank { existingData["department"] ?: "" })
                    put("salary", salary ?: existingData["salary"]?.toFloat())
                    put("joiningdate", joiningDate.ifBlank { existingData["joiningdate"] ?: "" })
                    put("leaves", existingData["leaves"]?.toInt()) // Retain existing leaves
                }

                val client = OkHttpClient()
                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("http://10.224.41.11/comp2000/employees/edit/$id")
                    .put(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Employee updated successfully", Toast.LENGTH_SHORT).show()
                        showNotification("Employee Edited", "Employee with ID $id has been successfully updated.")
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update employee", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Fetches employee details from the server - Used so in case a field is missing in the editting brackets, it can prevent data from being lost (or in other words, converted into "null")
    private fun fetchEmployeeData(id: String): Map<String, String>? {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.224.41.11/comp2000/employees/get/$id")
                .get()
                .build()

            val response = client.newCall(request).execute()
            return if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                mapOf(
                    "firstname" to jsonResponse.getString("firstname"),
                    "lastname" to jsonResponse.getString("lastname"),
                    "email" to jsonResponse.getString("email"),
                    "department" to jsonResponse.getString("department"),
                    "salary" to jsonResponse.getString("salary"),
                    "joiningdate" to convertToIsoDate(jsonResponse.getString("joiningdate")),
                    "leaves" to jsonResponse.getString("leaves")
                )
            } else null
        } catch (e: Exception) {
            return null
        }
    }
    // Handles the conversion of joiningdate to a valid format for the payload, yyyy-MM-dd
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

    // Handles notifications for editing employees
    private fun showNotification(title: String, message: String) {
        val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isPushNotificationsEnabled = sharedPreferences.getBoolean("push_notifications_enabled", false)

        if (!isPushNotificationsEnabled) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Notification permission is not granted", Toast.LENGTH_SHORT).show()
            return
        }

        // Creates a notification channel (Which is required for Android 8.0+)
        val channelId = "employee_notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Employee Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(requireContext()).notify(System.currentTimeMillis().toInt(), notification)
    }
}
