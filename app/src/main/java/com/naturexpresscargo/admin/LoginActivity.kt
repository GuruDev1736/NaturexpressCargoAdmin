package com.naturexpresscargo.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.naturexpresscargo.admin.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupTextWatchers()

        // Login button click
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateLogin(email, password)) {
                loginUser(email, password)
            }
        }

        // Forgot password click
        binding.forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun setupTextWatchers() {
        // Clear error when user starts typing
        binding.emailInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.emailInput.error = null
            }
        }

        binding.passwordInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.passwordInput.error = null
            }
        }
    }

    private fun validateLogin(email: String, password: String): Boolean {
        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            binding.emailInput.error = "Email is required"
            binding.emailInput.requestFocus()
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Invalid email format (e.g., user@example.com)"
            binding.emailInput.requestFocus()
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            binding.passwordInput.error = "Password is required"
            if (isValid) binding.passwordInput.requestFocus()
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInput.error = "Password must be at least 6 characters"
            if (isValid) binding.passwordInput.requestFocus()
            isValid = false
        }

        return isValid
    }

    private fun loginUser(email: String, password: String) {
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "Logging in..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    // Check user role and navigate accordingly
                    checkUserRoleAndNavigate()
                } else {
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = "Login"
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun checkUserRoleAndNavigate() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val database = com.google.firebase.database.FirebaseDatabase.getInstance()
            database.getReference("users").child(currentUser.uid)
                .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        val role = snapshot.child("role").getValue(String::class.java)

                        if (role == "ROLE_ADMIN") {
                            startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                            finish()
                        } else {
                            // Not an admin, sign out and show error
                            auth.signOut()
                            binding.loginButton.isEnabled = true
                            binding.loginButton.text = "Login"
                            Toast.makeText(
                                this@LoginActivity,
                                "Access denied. Admin credentials required.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        auth.signOut()
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "Login"
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }
}
