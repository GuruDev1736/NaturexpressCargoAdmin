package com.naturexpresscargo.admin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.admin.adapters.RequestsAdapter
import com.naturexpresscargo.admin.databinding.FragmentAdminRequestsBinding
import com.naturexpresscargo.admin.models.ServiceRequest

class AdminRequestsFragment : Fragment() {

    private var _binding: FragmentAdminRequestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var requestsAdapter: RequestsAdapter
    private val requestsList = mutableListOf<ServiceRequest>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        loadRequests()
    }

    private fun setupRecyclerView() {
        requestsAdapter = RequestsAdapter(
            requests = requestsList,
            onUpdateStatus = { request ->
                showUpdateStatusDialog(request)
            },
            onDelete = { request ->
                showDeleteConfirmationDialog(request)
            }
        )
        binding.requestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = requestsAdapter
        }
    }

    private fun loadRequests() {
        _binding?.progressBar?.visibility = View.VISIBLE

        database.getReference("requests")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if view is still available
                    if (_binding == null) return

                    requestsList.clear()

                    for (requestSnapshot in snapshot.children) {
                        val request = requestSnapshot.getValue(ServiceRequest::class.java)
                        request?.let { requestsList.add(it) }
                    }

                    requestsList.sortByDescending { it.createdAt }

                    _binding?.progressBar?.visibility = View.GONE

                    if (requestsList.isEmpty()) {
                        _binding?.emptyStateLayout?.visibility = View.VISIBLE
                        _binding?.requestsRecyclerView?.visibility = View.GONE
                    } else {
                        _binding?.emptyStateLayout?.visibility = View.GONE
                        _binding?.requestsRecyclerView?.visibility = View.VISIBLE
                        requestsAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Check if view is still available
                    if (_binding == null) return

                    _binding?.progressBar?.visibility = View.GONE
                    _binding?.emptyStateLayout?.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Failed to load requests: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showUpdateStatusDialog(request: ServiceRequest) {
        val statuses = arrayOf("pending", "confirmed", "in_transit", "delivered", "cancelled")
        val statusLabels = arrayOf("Pending", "Confirmed", "In Transit", "Delivered", "Cancelled")

        val currentIndex = statuses.indexOf(request.status)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Update Request Status")
            .setSingleChoiceItems(statusLabels, currentIndex) { dialog, which ->
                val newStatus = statuses[which]
                updateRequestStatus(request, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateRequestStatus(request: ServiceRequest, newStatus: String) {
        val updatedRequest = request.copy(status = newStatus)

        database.getReference("requests").child(request.requestId)
            .setValue(updatedRequest)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Status updated to ${getStatusLabel(newStatus)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to update status: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showDeleteConfirmationDialog(request: ServiceRequest) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Request")
            .setMessage("Are you sure you want to delete this request?")
            .setPositiveButton("Delete") { _, _ ->
                deleteRequest(request)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteRequest(request: ServiceRequest) {
        database.getReference("requests").child(request.requestId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Request deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to delete request: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getStatusLabel(status: String): String {
        return when (status) {
            "pending" -> "Pending"
            "confirmed" -> "Confirmed"
            "in_transit" -> "In Transit"
            "delivered" -> "Delivered"
            "cancelled" -> "Cancelled"
            else -> status
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

