package com.naturexpresscargo.admin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.admin.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Navigate after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndNavigate()
        }, 3000)
    }

    private fun checkUserAndNavigate() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is logged in, check their role
            checkUserRole(currentUser.uid)
        } else {
            // User is not logged in, go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun checkUserRole(userId: String) {
        database.getReference("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.child("role").getValue(String::class.java)

                    if (role == "ROLE_ADMIN") {
                        // Navigate to Admin Dashboard
                        startActivity(Intent(this@SplashActivity, AdminDashboardActivity::class.java))
                        finish()
                    } else {
                        // Not an admin, sign out and go to login
                        auth.signOut()
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // On error, sign out and go to login
                    auth.signOut()
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
            })
    }
}
