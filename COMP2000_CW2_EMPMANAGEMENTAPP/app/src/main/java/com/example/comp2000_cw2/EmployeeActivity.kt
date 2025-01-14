package com.example.comp2000_cw2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// This serves as the main entry point when logging-in as an Employee. Sets up the main layout for Employees
class EmployeeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }
}