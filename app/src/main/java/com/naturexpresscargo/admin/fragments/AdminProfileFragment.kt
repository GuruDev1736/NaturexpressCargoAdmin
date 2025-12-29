package com.naturexpresscargo.admin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.admin.LoginActivity
import com.naturexpresscargo.admin.databinding.FragmentAdminProfileBinding

class AdminProfileFragment : Fragment() {

    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadAdminProfile()
        setupClickListeners()
    }

    private fun loadAdminProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        database.getReference("users").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown Admin"
                    val email = snapshot.child("email").getValue(String::class.java) ?: currentUser.email ?: "No email"
                    val phone = snapshot.child("phone").getValue(String::class.java) ?: "Not provided"

                    binding.userName.text = name
                    binding.userEmail.text = email
                    binding.userPhone.text = phone
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load profile: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupClickListeners() {
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                auth.signOut()
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently deleted.")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteAccount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteAccount() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid

        // Show progress
        Toast.makeText(requireContext(), "Deleting account...", Toast.LENGTH_SHORT).show()

        // Delete user data from database
        database.getReference("users").child(userId).removeValue()
            .addOnSuccessListener {
                // Delete all user's requests
                database.getReference("requests")
                    .orderByChild("userId")
                    .equalTo(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (requestSnapshot in snapshot.children) {
                                requestSnapshot.ref.removeValue()
                            }

                            // Delete the Firebase Auth account
                            currentUser.delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Account deleted successfully",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // Navigate to login screen
                                    val intent = Intent(requireContext(), LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    requireActivity().finish()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to delete account: ${exception.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to delete user data: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to delete user data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

