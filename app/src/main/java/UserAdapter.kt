package com.example.securepoint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var userList: List<User>,
    private val onRoleToggle: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val email = itemView.findViewById<TextView>(R.id.emailText)
        val role = itemView.findViewById<TextView>(R.id.roleText)
        val toggleBtn = itemView.findViewById<Button>(R.id.toggleButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount() = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.email.text = user.email
        holder.role.text = "Role: ${user.role}"
        holder.toggleBtn.text = if (user.role == "admin") "Demote to Guest" else "Promote to Admin"

        holder.toggleBtn.setOnClickListener {
            onRoleToggle(user)
        }
    }

    fun updateList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}
