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
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


// Provides the necessary UI and functionality for administrators to add a new employee to the API
class AdminAddEmployeeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflates the fragment layout for adding a new employee
        return inflater.inflate(R.layout.fragment_add_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Binds UI elements
        val fullNameEditText = view.findViewById<EditText>(R.id.full_name)
        val emailEditText = view.findViewById<EditText>(R.id.email)
        val departmentEditText = view.findViewById<EditText>(R.id.department)
        val salaryEditText = view.findViewById<EditText>(R.id.salary)
        val joiningDateEditText = view.findViewById<EditText>(R.id.joining_date)
        val submitButton = view.findViewById<Button>(R.id.submit_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        // Handles the functionality of the submit button - So once its clicked, it adds the new Employee to the database.
        submitButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val department = departmentEditText.text.toString()
            val salary = salaryEditText.text.toString().toFloatOrNull()
            val joiningDate = joiningDateEditText.text.toString()

            // Error handling
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

            // Splits the full name into firstname and lastname, since those values are required in the API
            val (firstname, lastname) = fullName.split(" ", limit = 2).let {
                Pair(it.getOrElse(0) { "" }, it.getOrElse(1) { "" })
            }

            // Calls the method to add the employee
            addEmployee(firstname, lastname, email, department, salary, joiningDate)
        }

        // Handles Cancel button so once its clicked, it goes back to the previous fragment
        cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    // Sends a request to the server to add this new employee
    private fun addEmployee(firstname: String, lastname: String, email: String, department: String, salary: Float, joiningDate: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                // Creates the JSON payload for the new employee about to be added
                val json = JSONObject().apply {
                    put("firstname", firstname)
                    put("lastname", lastname)
                    put("email", email)
                    put("department", department)
                    put("salary", salary)
                    put("joiningdate", joiningDate)
                }

                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("http://10.224.41.11/comp2000/employees/add")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Employee added successfully", Toast.LENGTH_SHORT).show()
                        showNotification("Employee Added", "A new employee has been successfully added.")
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Failed to add employee", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Handles notifications for adding employees
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
