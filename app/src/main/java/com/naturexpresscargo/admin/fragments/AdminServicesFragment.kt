package com.naturexpresscargo.admin.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.admin.AddServiceActivity
import com.naturexpresscargo.admin.EditServiceActivity
import com.naturexpresscargo.admin.adapters.ServicesAdapter
import com.naturexpresscargo.admin.databinding.FragmentAdminServicesBinding
import com.naturexpresscargo.admin.models.Service

class AdminServicesFragment : Fragment() {

    private var _binding: FragmentAdminServicesBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var servicesAdapter: ServicesAdapter
    private val servicesList = mutableListOf<Service>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        loadServices()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        servicesAdapter = ServicesAdapter(
            services = servicesList,
            onEditClick = { service ->
                val intent = Intent(requireContext(), EditServiceActivity::class.java)
                intent.putExtra("service", service)
                startActivity(intent)
            },
            onStatusToggle = { service ->
                toggleServiceStatus(service)
            }
        )
        binding.servicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = servicesAdapter
        }
    }

    private fun setupClickListeners() {
        binding.addServiceButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddServiceActivity::class.java))
        }
    }

    private fun toggleServiceStatus(service: Service) {
        val updatedService = service.copy(active = !service.active)

        database.getReference("services").child(service.serviceId)
            .setValue(updatedService)
            .addOnSuccessListener {
                val status = if (updatedService.active) "activated" else "deactivated"
                Log.d("AdminServicesFragment", "Service status updated to $status")
            }
            .addOnFailureListener { error ->
                android.widget.Toast.makeText(
                    requireContext(),
                    "Failed to update status: ${error.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadServices() {
        _binding?.progressBar?.visibility = View.VISIBLE

        database.getReference("services")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if view is still available
                    if (_binding == null) return

                    servicesList.clear()

                    for (serviceSnapshot in snapshot.children) {
                        val service = serviceSnapshot.getValue(Service::class.java)
                        service?.let { servicesList.add(it) }
                    }

                    _binding?.progressBar?.visibility = View.GONE

                    if (servicesList.isEmpty()) {
                        _binding?.emptyStateLayout?.visibility = View.VISIBLE
                        _binding?.servicesRecyclerView?.visibility = View.GONE
                    } else {
                        _binding?.emptyStateLayout?.visibility = View.GONE
                        _binding?.servicesRecyclerView?.visibility = View.VISIBLE
                        servicesAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Check if view is still available
                    if (_binding == null) return

                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.emptyStateLayout?.visibility = View.VISIBLE
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

