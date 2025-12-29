package com.naturexpresscargo.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.naturexpresscargo.admin.databinding.ActivityContactUsBinding
import com.naturexpresscargo.admin.models.ContactEnquiry

class ContactUsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactUsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Handle window insets - apply top padding to toolbar
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0
            )
            insets
        }

        // Handle bottom insets for navigation bar - find ScrollView by findViewById
        val scrollView = findViewById<android.widget.ScrollView>(R.id.scroll_view)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupClickListeners()
        loadUserData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Contact Us"

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.getReference("users").child(currentUser.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val phone = snapshot.child("phone").getValue(String::class.java)

                    // Pre-fill the form with user data
                    name?.let { binding.nameInput.setText(it) }
                    email?.let { binding.emailInput.setText(it) }
                    phone?.let { binding.phoneInput.setText(it) }
                }
        }
    }

    private fun setupClickListeners() {
        binding.sendEnquiryButton.setOnClickListener {
            submitEnquiry()
        }
    }

    private fun submitEnquiry() {
        // Get input values
        val name = binding.nameInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val packages = binding.packagesInput.text.toString().trim()
        val weight = binding.weightInput.text.toString().trim()
        val from = binding.fromInput.text.toString().trim()
        val to = binding.toInput.text.toString().trim()
        val message = binding.messageInput.text.toString().trim()

        // Validate inputs
        if (name.isEmpty()) {
            binding.nameInput.error = "Name is required"
            binding.nameInput.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            binding.phoneInput.error = "Phone number is required"
            binding.phoneInput.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.emailInput.error = "Email is required"
            binding.emailInput.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Invalid email address"
            binding.emailInput.requestFocus()
            return
        }

        if (packages.isEmpty()) {
            binding.packagesInput.error = "Number of packages is required"
            binding.packagesInput.requestFocus()
            return
        }

        if (weight.isEmpty()) {
            binding.weightInput.error = "Item weight is required"
            binding.weightInput.requestFocus()
            return
        }

        if (from.isEmpty()) {
            binding.fromInput.error = "From location is required"
            binding.fromInput.requestFocus()
            return
        }

        if (to.isEmpty()) {
            binding.toInput.error = "To location is required"
            binding.toInput.requestFocus()
            return
        }

        // Disable button to prevent multiple submissions
        binding.sendEnquiryButton.isEnabled = false
        binding.sendEnquiryButton.text = "Sending..."

        // Create enquiry object
        val enquiryId = database.getReference("enquiries").push().key ?: ""
        val currentUser = auth.currentUser

        val enquiry = ContactEnquiry(
            id = enquiryId,
            name = name,
            phoneNumber = phone,
            email = email,
            numberOfPackages = packages,
            itemWeight = weight,
            fromLocation = from,
            toLocation = to,
            message = message,
            timestamp = System.currentTimeMillis(),
            userId = currentUser?.uid ?: ""
        )

        // Save to Firebase
        database.getReference("enquiries").child(enquiryId)
            .setValue(enquiry)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Enquiry sent successfully! We'll get back to you soon.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to send enquiry: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.sendEnquiryButton.isEnabled = true
                binding.sendEnquiryButton.text = "Send Enquiry"
            }
    }
}

