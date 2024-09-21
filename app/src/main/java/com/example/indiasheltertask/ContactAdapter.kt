package com.example.indiasheltertask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.indiasheltertask.models.ContactModel


class ContactAdapter(
    val contactList: ArrayList<ContactModel>,
    private val listener: OnItemClickListener // Passing the listener interface
) : RecyclerView.Adapter<ContactAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val userImg: TextView = itemView.findViewById(R.id.img_user)
        val userName: TextView = itemView.findViewById(R.id.name)
        val userPhone: TextView = itemView.findViewById(R.id.phone)
        val userWhats: ImageView = itemView.findViewById(R.id.img_whats)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position)
                }
            }

            // WhatsApp icon click listener
            userWhats.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onWhatsAppClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = contactList[position]
        holder.userName.text = item.displayName
        holder.userPhone.text = item.number
        holder.userImg.text = item.initials
    }

    override fun getItemCount(): Int = contactList.size

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onWhatsAppClick(position: Int)
        fun onPermissionsDenied(requestCode: Int, perms: List<String>)
        fun onPermissionsGranted(requestCode: Int, perms: List<String>)
    }
}
