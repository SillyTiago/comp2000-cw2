package com.example.comp2000_cw2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.comp2000_cw2.databinding.LoginBinding

// Handles user authentication and navigation.
// Users are able to log-in and access their respective page, depending on what role they are in the local database
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding // View binding for login layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflates the login layout
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // handles log-in button click
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
