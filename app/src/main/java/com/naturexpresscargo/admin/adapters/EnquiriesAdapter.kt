package com.naturexpresscargo.admin.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naturexpresscargo.admin.databinding.ItemEnquiryBinding
import com.naturexpresscargo.admin.models.ContactEnquiry
import java.text.SimpleDateFormat
import java.util.*

class EnquiriesAdapter(
    private val enquiries: List<ContactEnquiry>
) : RecyclerView.Adapter<EnquiriesAdapter.EnquiryViewHolder>() {

    class EnquiryViewHolder(val binding: ItemEnquiryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnquiryViewHolder {
        val binding = ItemEnquiryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EnquiryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EnquiryViewHolder, position: Int) {
        val enquiry = enquiries[position]

        holder.binding.apply {
            // Customer details
            enquiryName.text = enquiry.name
            enquiryPhone.text = enquiry.phoneNumber
            enquiryEmail.text = enquiry.email
            enquiryDate.text = formatDate(enquiry.timestamp)

            // Enquiry details
            enquiryPackages.text = enquiry.numberOfPackages.ifEmpty { "-" }
            enquiryWeight.text = if (enquiry.itemWeight.isNotEmpty()) "${enquiry.itemWeight} kg" else "-"
            enquiryFrom.text = enquiry.fromLocation.ifEmpty { "-" }
            enquiryTo.text = enquiry.toLocation.ifEmpty { "-" }
            enquiryMessage.text = enquiry.message.ifEmpty { "No message provided" }

            // Email button click
            emailButton.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${enquiry.email}")
                    putExtra(Intent.EXTRA_SUBJECT, "Regarding your enquiry")
                    putExtra(Intent.EXTRA_TEXT, "Dear ${enquiry.name},\n\n")
                }
                root.context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
            }

            // Call button click
            callButton.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${enquiry.phoneNumber}")
                }
                root.context.startActivity(callIntent)
            }
        }
    }

    override fun getItemCount(): Int = enquiries.size

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

