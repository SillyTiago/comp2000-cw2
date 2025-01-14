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
import okhttp3.OkHttpClient
import okhttp3.Request
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// Provides the necessary UI and functionality for administrators to delete an existing employee in the API
class AdminDeleteEmployeeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflates the fragment layout for deleting an existing employee
        return inflater.inflate(R.layout.fragment_admin_delete_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Binds UI elements
        val employeeIdEditText = view.findViewById<EditText>(R.id.employee_id)
        val submitButton = view.findViewById<Button>(R.id.submit_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        // Handles the functionality of the submit button - So once its clicked, it deletes the Employee from the Database.
        submitButton.setOnClickListener {
            val employeeId = employeeIdEditText.text.toString().trim()

            // Error Handling
            val empId = employeeId.toIntOrNull()
            if (empId == null || empId <= 0) {
                Toast.makeText(requireContext(), "Valid EmployeeID is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calls the method to delete the Employee
            deleteEmployee(employeeId)
        }

        // Handles Cancel button so once its clicked, it goes back to the previous fragment
        cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    // Sends a request to the API server to delete the specified Employee.
    private fun deleteEmployee(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("http://10.224.41.11/comp2000/employees/delete/$id")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Employee deleted successfully", Toast.LENGTH_SHORT).show()
                        showNotification("Employee Deleted", "Employee with ID $id has been successfully deleted.")
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete employee", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Handles notifications for deleting employees
    private fun showNotification(title: String, message: String) {
        val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isPushNotificationsEnabled = sharedPreferences.getBoolean("push_notifications_enabled", false)

        if (!isPushNotificationsEnabled) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Checks if the notification permission is granted
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(requireContext(), "Notification permission is not granted", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Creates a notification channel (Which is required for Android 8.0+)
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

        // Builds the notification
        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_menu_delete) // Use an appropriate icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Shows the notification
        NotificationManagerCompat.from(requireContext()).notify(System.currentTimeMillis().toInt(), notification)
    }
}
