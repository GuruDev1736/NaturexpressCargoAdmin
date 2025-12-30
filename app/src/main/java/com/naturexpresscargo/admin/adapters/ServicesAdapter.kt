package com.naturexpresscargo.admin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.naturexpresscargo.admin.R
import com.naturexpresscargo.admin.databinding.ItemServiceBinding
import com.naturexpresscargo.admin.models.Service
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServicesAdapter(
    private val services: List<Service>,
    private val onEditClick: (Service) -> Unit,
    private val onStatusToggle: (Service) -> Unit
) : RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(val binding: ItemServiceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]

        holder.binding.apply {
            serviceNameText.text = service.serviceName
            servicePriceText.text = "₹${service.pricePerWeight}/kg"
            serviceDescriptionText.text = service.description
            serviceDateText.text = formatDate(service.createdAt)

            if (service.active) {
                statusLabel.text = "Active"
                statusSwitch.trackTintList = ContextCompat.getColorStateList(
                    root.context,
                    R.color.success_green
                )
                statusLabel.setTextColor(
                    ContextCompat.getColor(root.context, R.color.success_green)
                )
                statusSwitch.isChecked = true
            } else {
                statusLabel.text = "Inactive"
                statusSwitch.trackTintList = ContextCompat.getColorStateList(
                    root.context,
                    R.color.error_red
                )
                statusLabel.setTextColor(
                    ContextCompat.getColor(root.context, R.color.text_hint)
                )
                statusSwitch.isChecked = false
            }

            // Set new listener
            statusSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    statusLabel.text = "Active"
                    statusLabel.setTextColor(
                        ContextCompat.getColor(root.context, R.color.success_green)
                    )
                    onStatusToggle(service)
                } else {
                    statusLabel.text = "Inactive"
                    statusLabel.setTextColor(
                        ContextCompat.getColor(root.context, R.color.text_hint)
                    )
                    onStatusToggle(service)
                }
            }

            // Edit button click
            editButton.setOnClickListener {
                onEditClick(service)
            }
        }
    }

    override fun getItemCount(): Int = services.size

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
