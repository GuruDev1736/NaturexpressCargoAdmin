package com.naturexpresscargo.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.admin.adapters.EnquiriesAdapter
import com.naturexpresscargo.admin.databinding.ActivityViewEnquiriesBinding
import com.naturexpresscargo.admin.models.ContactEnquiry

class ViewEnquiriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewEnquiriesBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var enquiriesAdapter: EnquiriesAdapter
    private val enquiriesList = mutableListOf<ContactEnquiry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityViewEnquiriesBinding.inflate(layoutInflater)
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

        // Handle bottom insets for navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentFrame) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance()

        setupToolbar()
        setupRecyclerView()
        loadEnquiries()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Contact Enquiries"

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        enquiriesAdapter = EnquiriesAdapter(enquiriesList)
        binding.enquiriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ViewEnquiriesActivity)
            adapter = enquiriesAdapter
        }
    }

    private fun loadEnquiries() {
        binding.progressBar.visibility = View.VISIBLE

        database.getReference("enquiries")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    enquiriesList.clear()

                    for (enquirySnapshot in snapshot.children) {
                        val enquiry = enquirySnapshot.getValue(ContactEnquiry::class.java)
                        enquiry?.let { enquiriesList.add(it) }
                    }

                    // Sort by timestamp (newest first)
                    enquiriesList.sortByDescending { it.timestamp }

                    binding.progressBar.visibility = View.GONE

                    if (enquiriesList.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.enquiriesRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.enquiriesRecyclerView.visibility = View.VISIBLE
                        enquiriesAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                }
            })
    }
}

