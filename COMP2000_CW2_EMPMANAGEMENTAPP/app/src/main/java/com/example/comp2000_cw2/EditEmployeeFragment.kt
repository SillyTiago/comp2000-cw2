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
import java.text.SimpleDateFormat
import java.util.Locale
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// Provides the necessary UI and functionality for employees to edit their details, specifically their full name.
class EditEmployeeFragment : Fragment() {

    private var employeeId: Int = 3164
    private var originalFullName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // inflates the layout for editing the employee.
        return inflater.inflate(R.layout.fragment_edit_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Binds UI elements
        val fullNameEditText = view.findViewById<EditText>(R.id.edit_full_name)
        val emailEditText = view.findViewById<EditText>(R.id.edit_email)
        val departmentEditText = view.findViewById<EditText>(R.id.edit_department)
        val salaryEditText = view.findViewById<EditText>(R.id.edit_salary)
        val joiningDateEditText = view.findViewById<EditText>(R.id.edit_joining_date)
        val leavesEditText = view.findViewById<EditText>(R.id.edit_leaves)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        // Fetches and displays the employee's existing details. This is done to prevent NULL values from being sent to the database.
        CoroutineScope(Dispatchers.IO).launch {
            val employeeData = fetchEmployeeData(employeeId)
            if (employeeData != null) {
                withContext(Dispatchers.Main) {
                    fullNameEditText.setText(
                        "${employeeData.getString("firstname")} ${employeeData.getString("lastname")}"
                    )
                    emailEditText.setText(employeeData.getString("email"))
                    departmentEditText.setText(employeeData.getString("department"))
                    salaryEditText.setText(employeeData.getString("salary"))
                    joiningDateEditText.setText(convertToIsoDate(employeeData.getString("joiningdate")))
                    leavesEditText.setText(employeeData.getInt("leaves").toString())
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to fetch employee data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        saveButton.setOnClickListener {
            // Collects and validates the input data, then updates the employee details
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val department = departmentEditText.text.toString()
            val salary = salaryEditText.text.toString()
            val joiningDate = joiningDateEditText.text.toString()
            val leaves = leavesEditText.text.toString()

            if (fullName.isBlank() || email.isBlank() || department.isBlank() || salary.isBlank() || joiningDate.isBlank() || leaves.isBlank()) {
                Toast.makeText(requireContext(), "All fields must be filled. If this is the case, it's most likely an Administrator has deleted them", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fullName == originalFullName) {
                Toast.makeText(requireContext(), "You must change the full name before saving", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val (firstname, lastname) = fullName.split(" ", limit = 2).let {
                Pair(it.getOrElse(0) { "" }, it.getOrElse(1) { "" })
            }

            updateEmployeeDetails(employeeId, firstname, lastname, email, department, salary.toFloat(), joiningDate, leaves.toInt())
        }

        cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    // Fetches the employees current details from the server
    private fun fetchEmployeeData(employeeId: Int): JSONObject? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.224.41.11/comp2000/employees/get/$employeeId")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    JSONObject(response.body!!.string())
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Handles the PUT request for updating the API server with the new Employee Details
    private fun updateEmployeeDetails(
        id: Int,
        firstname: String,
        lastname: String,
        email: String,
        department: String,
        salary: Float,
        joiningDate: String,
        leaves: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                val json = JSONObject().apply {
                    put("firstname", firstname)
                    put("lastname", lastname)
                    put("email", email)
                    put("department", department)
                    put("salary", salary)
                    put("joiningdate", joiningDate)
                    put("leaves", leaves)
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("http://10.224.41.11/comp2000/employees/edit/$id")
                    .put(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Details updated successfully", Toast.LENGTH_SHORT).show()
                        showNotification("Details updated", "Your Employee details have been updated")
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

    // Since the database retrieves JoiningDate in an unsupported format, we need to convert it into something the API server will accept, which is yyyy-MM-dd
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

    // Handles notifications for deleting employees
    private fun showNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the notification permission is granted
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(requireContext(), "Notification permission is not granted", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Create a notification channel (required for Android 8.0+)
        val channelId = "employee_notifications"
        val channelName = "Employee Notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_menu_delete) // Use an appropriate icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Show the notification
        NotificationManagerCompat.from(requireContext()).notify(System.currentTimeMillis().toInt(), notification)
    }
}

