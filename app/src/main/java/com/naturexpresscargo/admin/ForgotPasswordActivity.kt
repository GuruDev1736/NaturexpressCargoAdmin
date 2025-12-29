package com.naturexpresscargo.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.naturexpresscargo.admin.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Forgot Password"

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Send reset email button click
        binding.sendResetEmailButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()

            if (validateEmail(email)) {
                sendPasswordResetEmail(email)
            }
        }

        // Back to login click
        binding.backToLoginText.setOnClickListener {
            finish()
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.emailInput.error = "Email is required"
            binding.emailInput.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Please enter a valid email"
            binding.emailInput.requestFocus()
            return false
        }

        return true
    }

    private fun sendPasswordResetEmail(email: String) {
        binding.sendResetEmailButton.isEnabled = false
        binding.sendResetEmailButton.text = "Sending..."

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.sendResetEmailButton.isEnabled = true
                binding.sendResetEmailButton.text = "Send Reset Link"

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset email sent to $email. Please check your inbox.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Show success message
                    binding.successMessage.visibility = android.view.View.VISIBLE
                    binding.emailLayout.visibility = android.view.View.GONE
                    binding.sendResetEmailButton.visibility = android.view.View.GONE
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send reset email: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}

