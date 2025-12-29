package com.naturexpresscargo.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.naturexpresscargo.admin.databinding.ActivityAddServiceBinding
import com.naturexpresscargo.admin.models.Service

class AddServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddServiceBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityAddServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.submitButton.setOnClickListener {
            val serviceName = binding.serviceNameInput.text.toString().trim()
            val pricePerWeight = binding.servicePriceInput.text.toString().trim()
            val description = binding.serviceDescriptionInput.text.toString().trim()

            if (validateInputs(serviceName, pricePerWeight, description)) {
                saveService(serviceName, pricePerWeight.toDouble(), description)
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(name: String, price: String, description: String): Boolean {
        if (name.isEmpty()) {
            binding.serviceNameInput.error = "Service name is required"
            binding.serviceNameInput.requestFocus()
            return false
        }

        if (price.isEmpty()) {
            binding.servicePriceInput.error = "Price per weight is required"
            binding.servicePriceInput.requestFocus()
            return false
        }

        if (price.toDoubleOrNull() == null || price.toDouble() <= 0) {
            binding.servicePriceInput.error = "Please enter a valid price"
            binding.servicePriceInput.requestFocus()
            return false
        }

        if (description.isEmpty()) {
            binding.serviceDescriptionInput.error = "Description is required"
            binding.serviceDescriptionInput.requestFocus()
            return false
        }

        return true
    }

    private fun saveService(name: String, price: Double, description: String) {
        binding.submitButton.isEnabled = false
        binding.submitButton.text = "Saving..."

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            binding.submitButton.isEnabled = true
            binding.submitButton.text = "Submit"
            return
        }

        val serviceId = database.getReference("services").push().key ?: return

        val service = Service(
            serviceId = serviceId,
            serviceName = name,
            pricePerWeight = price,
            description = description,
            createdBy = currentUser.uid,
            createdAt = System.currentTimeMillis(),
            active = true
        )

        database.getReference("services").child(serviceId).setValue(service)
            .addOnSuccessListener {
                Toast.makeText(this, "Service added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Failed to add service: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.submitButton.isEnabled = true
                binding.submitButton.text = "Submit"
            }
    }
}
