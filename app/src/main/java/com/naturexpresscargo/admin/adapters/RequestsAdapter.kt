package com.naturexpresscargo.admin.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.naturexpresscargo.admin.R
import com.naturexpresscargo.admin.databinding.ItemRequestBinding
import com.naturexpresscargo.admin.models.ServiceRequest
import java.text.SimpleDateFormat
import java.util.*

class RequestsAdapter(
    private val requests: List<ServiceRequest>,
    private val onUpdateStatus: (ServiceRequest) -> Unit,
    private val onDelete: (ServiceRequest) -> Unit
) : RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {

    class RequestViewHolder(val binding: ItemRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]

        holder.binding.apply {
            // Service and user info
            serviceNameText.text = request.serviceName
            userNameText.text = request.userName
            userEmailText.text = request.userEmail
            userPhoneText.text = request.userPhone

            // Weight and price
            weightText.text = "${request.weight} kg"
            pricePerKgText.text = "₹${request.pricePerWeight}/kg"
            totalPriceText.text = "₹${String.format("%.2f", request.totalPrice)}"

            // Date
            dateText.text = formatDate(request.createdAt)

            // Cargo details (new fields)
            pickupAddressText.text = request.pickupAddress.ifEmpty { "-" }
            deliveryAddressText.text = request.deliveryAddress.ifEmpty { "-" }
            cargoTypeText.text = request.cargoType.ifEmpty { "-" }
            cargoDescriptionText.text = request.cargoDescription.ifEmpty { "-" }
            numberOfPackagesText.text = request.numberOfPackages.ifEmpty { "-" }
            transportModeText.text = request.transportMode.ifEmpty { "-" }
            pickupDateText.text = request.pickupDate.ifEmpty { "-" }
            contactNumberText.text = request.contactNumber.ifEmpty { "-" }

            // Expandable cargo details section
            var isExpanded = false
            cargoDetailsHeader.setOnClickListener {
                isExpanded = !isExpanded
                if (isExpanded) {
                    cargoDetailsContent.visibility = android.view.View.VISIBLE
                    expandIcon.text = "▲"
                } else {
                    cargoDetailsContent.visibility = android.view.View.GONE
                    expandIcon.text = "▼"
                }
            }

            // Status badge
            when (request.status) {
                "pending" -> {
                    statusBadge.setBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.warning_orange)
                    )
                    statusText.text = "Pending"
                }
                "confirmed" -> {
                    statusBadge.setBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.info_blue)
                    )
                    statusText.text = "Confirmed"
                }
                "in_transit" -> {
                    statusBadge.setBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.primary_green)
                    )
                    statusText.text = "In Transit"
                }
                "delivered" -> {
                    statusBadge.setBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.success_green)
                    )
                    statusText.text = "Delivered"
                }
                "cancelled" -> {
                    statusBadge.setBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.error_red)
                    )
                    statusText.text = "Cancelled"
                }
                else -> {
                    statusBadge.setBackgroundColor(
                        ContextCompat.getColor(root.context, R.color.text_hint)
                    )
                    statusText.text = request.status
                }
            }

            // Email button click - Open email app
            emailButton.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${request.userEmail}")
                    putExtra(Intent.EXTRA_SUBJECT, "Regarding: ${request.serviceName}")
                    putExtra(Intent.EXTRA_TEXT, "Dear ${request.userName},\n\n")
                }
                root.context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
            }

            // Call button click - Open dialer
            callButton.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${request.userPhone}")
                }
                root.context.startActivity(callIntent)
            }

            // Update Status button click
            updateStatusButton.setOnClickListener {
                onUpdateStatus(request)
            }

            // Delete button click
            deleteButton.setOnClickListener {
                onDelete(request)
            }
        }
    }

    override fun getItemCount(): Int = requests.size

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

