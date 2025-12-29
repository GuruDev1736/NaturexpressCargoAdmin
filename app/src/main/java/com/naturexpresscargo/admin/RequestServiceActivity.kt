package com.naturexpresscargo.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.admin.databinding.ActivityRequestServiceBinding
import com.naturexpresscargo.admin.models.Service
import com.naturexpresscargo.admin.models.ServiceRequest
import java.text.SimpleDateFormat
import java.util.*

class RequestServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestServiceBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var service: Service
    private var calculatedWeight = 0.0
    private var calculatedTotalPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityRequestServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get service from intent
        @Suppress("DEPRECATION")
        service = intent.getSerializableExtra("service") as? Service ?: run {
            Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupUI()
        setupListeners()
        loadUserData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Request Service"

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupUI() {
        binding.serviceNameHeader.text = service.serviceName
        binding.pricePerKgHeader.text = "₹${service.pricePerWeight}/kg"
        binding.resultPricePerKg.text = "₹${service.pricePerWeight}"

        // Setup transport mode dropdown
        val transportModes = arrayOf("Road", "Rail", "Air", "Sea", "Multi-Modal")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, transportModes)
        binding.transportModeInput.setAdapter(adapter)
    }

    private fun setupListeners() {
        // Auto-calculate price when weight changes
        binding.actualWeightInput.doAfterTextChanged {
            calculatePrice()
        }

        // Date picker for pickup date
        binding.pickupDateInput.setOnClickListener {
            showDatePicker()
        }

        // Submit button
        binding.submitRequestButton.setOnClickListener {
            submitRequest()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        database.getReference("users").child(currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val phone = snapshot.child("phone").getValue(String::class.java)
                phone?.let { binding.contactNumberInput.setText(it) }
            }
    }

    private fun calculatePrice() {
        val weightStr = binding.actualWeightInput.text.toString().trim()

        if (weightStr.isEmpty()) {
            binding.resultWeight.text = "0 kg"
            binding.resultTotalPrice.text = "₹0"
            calculatedWeight = 0.0
            calculatedTotalPrice = 0.0
            return
        }

        val weight = weightStr.toDoubleOrNull()
        if (weight == null || weight <= 0) {
            binding.resultWeight.text = "0 kg"
            binding.resultTotalPrice.text = "₹0"
            calculatedWeight = 0.0
            calculatedTotalPrice = 0.0
            return
        }

        calculatedWeight = weight
        calculatedTotalPrice = weight * service.pricePerWeight

        binding.resultWeight.text = "$weight kg"
        binding.resultTotalPrice.text = "₹${String.format("%.2f", calculatedTotalPrice)}"
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.Theme_App_DatePicker,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.pickupDateInput.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun submitRequest() {
        // Get all input values
        val pickupAddress = binding.pickupAddressInput.text.toString().trim()
        val deliveryAddress = binding.deliveryAddressInput.text.toString().trim()
        val cargoType = binding.cargoTypeInput.text.toString().trim()
        val cargoDescription = binding.cargoDescriptionInput.text.toString().trim()
        val numberOfPackages = binding.numberOfPackagesInput.text.toString().trim()
        val actualWeight = binding.actualWeightInput.text.toString().trim()
        val transportMode = binding.transportModeInput.text.toString().trim()
        val pickupDate = binding.pickupDateInput.text.toString().trim()
        val contactNumber = binding.contactNumberInput.text.toString().trim()

        // Validate inputs
        if (pickupAddress.isEmpty()) {
            binding.pickupAddressInput.error = "Pickup address is required"
            binding.pickupAddressInput.requestFocus()
            return
        }

        if (deliveryAddress.isEmpty()) {
            binding.deliveryAddressInput.error = "Delivery address is required"
            binding.deliveryAddressInput.requestFocus()
            return
        }

        if (cargoType.isEmpty()) {
            binding.cargoTypeInput.error = "Cargo type is required"
            binding.cargoTypeInput.requestFocus()
            return
        }

        if (cargoDescription.isEmpty()) {
            binding.cargoDescriptionInput.error = "Cargo description is required"
            binding.cargoDescriptionInput.requestFocus()
            return
        }

        if (numberOfPackages.isEmpty()) {
            binding.numberOfPackagesInput.error = "Number of packages is required"
            binding.numberOfPackagesInput.requestFocus()
            return
        }

        if (actualWeight.isEmpty()) {
            binding.actualWeightInput.error = "Actual weight is required"
            binding.actualWeightInput.requestFocus()
            return
        }

        val weight = actualWeight.toDoubleOrNull()
        if (weight == null || weight <= 0) {
            binding.actualWeightInput.error = "Please enter a valid weight"
            binding.actualWeightInput.requestFocus()
            return
        }

        if (transportMode.isEmpty()) {
            binding.transportModeInput.error = "Transport mode is required"
            binding.transportModeInput.requestFocus()
            return
        }

        if (pickupDate.isEmpty()) {
            binding.pickupDateInput.error = "Pickup date is required"
            binding.pickupDateInput.requestFocus()
            return
        }

        if (contactNumber.isEmpty()) {
            binding.contactNumberInput.error = "Contact number is required"
            binding.contactNumberInput.requestFocus()
            return
        }

        // Disable button to prevent multiple submissions
        binding.submitRequestButton.isEnabled = false
        binding.submitRequestButton.text = "Submitting..."

        saveServiceRequest(
            pickupAddress,
            deliveryAddress,
            cargoType,
            cargoDescription,
            numberOfPackages,
            actualWeight,
            transportMode,
            pickupDate,
            contactNumber
        )
    }

    private fun saveServiceRequest(
        pickupAddress: String,
        deliveryAddress: String,
        cargoType: String,
        cargoDescription: String,
        numberOfPackages: String,
        actualWeight: String,
        transportMode: String,
        pickupDate: String,
        contactNumber: String
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            binding.submitRequestButton.isEnabled = true
            binding.submitRequestButton.text = "Submit Request"
            return
        }

        val requestId = database.getReference("requests").push().key ?: return

        database.getReference("users").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    val userName = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val userEmail = userSnapshot.child("email").getValue(String::class.java) ?: ""
                    val userPhone = userSnapshot.child("phone").getValue(String::class.java) ?: ""

                    val request = ServiceRequest(
                        requestId = requestId,
                        serviceId = service.serviceId,
                        serviceName = service.serviceName,
                        pricePerWeight = service.pricePerWeight,
                        userId = currentUser.uid,
                        userName = userName,
                        userEmail = userEmail,
                        userPhone = userPhone,
                        weight = calculatedWeight,
                        totalPrice = calculatedTotalPrice,
                        status = "pending",
                        createdAt = System.currentTimeMillis(),
                        pickupAddress = pickupAddress,
                        deliveryAddress = deliveryAddress,
                        cargoType = cargoType,
                        cargoDescription = cargoDescription,
                        numberOfPackages = numberOfPackages,
                        actualWeightKg = actualWeight,
                        transportMode = transportMode,
                        pickupDate = pickupDate,
                        contactNumber = contactNumber
                    )

                    database.getReference("requests").child(requestId)
                        .setValue(request)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@RequestServiceActivity,
                                "Request submitted successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this@RequestServiceActivity,
                                "Failed to submit request: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.submitRequestButton.isEnabled = true
                            binding.submitRequestButton.text = "Submit Request"
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@RequestServiceActivity,
                        "Failed to get user data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.submitRequestButton.isEnabled = true
                    binding.submitRequestButton.text = "Submit Request"
                }
            })
    }
}
