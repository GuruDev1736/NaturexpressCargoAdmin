package com.naturexpresscargo.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.naturexpresscargo.admin.databinding.ActivityEditServiceBinding
import com.naturexpresscargo.admin.models.Service

class EditServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditServiceBinding
    private lateinit var database: FirebaseDatabase
    private var service: Service? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityEditServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance()

        // Get service data from intent
        @Suppress("DEPRECATION")
        service = intent.getSerializableExtra("service") as? Service

        if (service == null) {
            Toast.makeText(this, "Error loading service", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        populateFields()
        setupClickListeners()
    }

    private fun populateFields() {
        service?.let {
            binding.serviceNameInput.setText(it.serviceName)
            binding.servicePriceInput.setText(it.pricePerWeight.toString())
            binding.serviceDescriptionInput.setText(it.description)
        }
    }

    private fun setupClickListeners() {
        binding.updateButton.setOnClickListener {
            val serviceName = binding.serviceNameInput.text.toString().trim()
            val pricePerWeight = binding.servicePriceInput.text.toString().trim()
            val description = binding.serviceDescriptionInput.text.toString().trim()

            if (validateInputs(serviceName, pricePerWeight, description)) {
                updateService(serviceName, pricePerWeight.toDouble(), description)
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

    private fun updateService(name: String, price: Double, description: String) {
        binding.updateButton.isEnabled = false
        binding.updateButton.text = "Updating..."

        val currentService = service ?: return

        val updatedService = currentService.copy(
            serviceName = name,
            pricePerWeight = price,
            description = description
        )

        database.getReference("services").child(currentService.serviceId)
            .setValue(updatedService)
            .addOnSuccessListener {
                Toast.makeText(this, "Service updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Failed to update service: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.updateButton.isEnabled = true
                binding.updateButton.text = "Update"
            }
    }
}
