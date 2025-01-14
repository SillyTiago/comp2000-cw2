package com.example.comp2000_cw2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.comp2000_cw2.databinding.LoginBinding
import com.example.comp2000_cw2.databinding.MainBinding
import android.view.Menu
import android.view.MenuItem

// This handles user authentication. Serves as the entry point to navigating to either sides of the application (employee and admin)
//
class MainActivity : AppCompatActivity() {

    // Binding for login layout, user validation and main layout
    private lateinit var loginBinding: LoginBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var mainBinding: MainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DatabaseHelper(this)
        loginBinding = LoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        // Handles the login button click in order to validate the user credentials (to give them access to the application)
        loginBinding.loginButton.setOnClickListener {
            val email = loginBinding.email.text.toString().trim()
            val password = loginBinding.password.text.toString().trim()

            val role = dbHelper.validateUser(email, password)
            if (role != null) {
                when (role) {
                    "employee" -> loadEmployeePage()
                    "admin" -> navigateToAdminPage()
                }
            } else {
                loginBinding.errorText.text = "Invalid email or password"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_user_preferences -> {
                true
            }
            R.id.menu_logout -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Loads employee dashboard and sets up navigation for everything employee-related.
    // It's only accessed IF current user is verified to be an employee
    private fun loadEmployeePage() {
        mainBinding = MainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        replaceFragment(EmployeeDetails())

        mainBinding.bottomNavigationView2.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.employee -> replaceFragment(EmployeeDetails())
                R.id.holidays -> replaceFragment(EmployeeHolidays())
                R.id.alerts -> replaceFragment(EmployeeAlerts())
                R.id.settings -> replaceFragment(SettingsFragment())
                else -> {}
            }
            true
        }
    }

    // Navigates to the administrator page by starting AdminActivity
    private fun navigateToAdminPage() {
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Handles the replacement of the current fragment with the specified fragment
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}