package com.naturexpresscargo.admin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.admin.ViewEnquiriesActivity
import com.naturexpresscargo.admin.databinding.FragmentAdminHomeBinding

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupUI()
        setupClickListeners()
        loadStats()
    }

    private fun setupClickListeners() {
        binding.viewEnquiriesCard.setOnClickListener {
            val intent = Intent(requireContext(), ViewEnquiriesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupUI() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.getReference("users").child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userName = snapshot.child("name").getValue(String::class.java)
                        binding.welcomeText.text = "Welcome Admin, $userName!"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.welcomeText.text = "Welcome Admin!"
                    }
                })
        }
    }

    private fun loadStats() {
        // Load total requests
        database.getReference("requests")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if view is still available
                    _binding?.totalRequests?.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // Load total services
        database.getReference("services")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if view is still available
                    _binding?.totalServices?.text = snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

