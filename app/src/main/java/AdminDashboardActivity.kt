package com.example.securepoint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.example.securepoint.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var userList: MutableList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        binding.btnManageNfc.setOnClickListener {
            startActivity(Intent(this, NfcTagManagerActivity::class.java))
        }

        setContentView(binding.root)

        // Initialize Firebase reference
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        // Setup RecyclerView
        userList = mutableListOf()
        adapter = UserAdapter(userList) { user ->
            toggleUserRole(user)
        }

        binding.usersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.usersRecyclerView.adapter = adapter

        fetchUsers()
    }

    private fun fetchUsers() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (child in snapshot.children) {
                    val rawUser = child.getValue(User::class.java)
                    if (rawUser != null) {
                        val user = User(
                            uid = child.key ?: "",
                            email = rawUser.email,
                            role = rawUser.role
                        )
                        userList.add(user)
                    }
                }
                adapter.updateList(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AdminDashboardActivity,
                    "Failed to load users: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun toggleUserRole(user: User) {
        val newRole = if (user.role == "admin") "guest" else "admin"
        dbRef.child(user.uid).child("role").setValue(newRole)
            .addOnSuccessListener {
                Toast.makeText(this, "${user.email} is now $newRole", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update role", Toast.LENGTH_SHORT).show()
            }
    }
}
