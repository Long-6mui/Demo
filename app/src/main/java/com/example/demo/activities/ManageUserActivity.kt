package com.example.demo.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.adapters.UserAdmin
import com.example.demo.adapters.UserAdapter
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.filter
import kotlin.text.contains
import kotlin.toString

class ManageUserActivity : AppCompatActivity() {

    private lateinit var adapter: UserAdapter
    private val fullUserList = mutableListOf<UserAdmin>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_user)

        val recyclerUsers = findViewById<RecyclerView>(R.id.recyclerUsers)
        val edtSearchUser = findViewById<EditText>(R.id.edtSearchUser)
        val btnBack = findViewById<ImageButton>(R.id.btnBackManageUser)

        // Xử lý nút Trở về
        btnBack.setOnClickListener {
            finish()
        }

        recyclerUsers.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(
            mutableListOf(),
            onBlockClick = { user, position -> toggleBlockUser(user) },
            onDeleteClick = { user, position -> showDeleteDialog(user) }
        )
        recyclerUsers.adapter = adapter

        loadUsersFromFirestore()

        edtSearchUser.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterUsers(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadUsersFromFirestore() {
        db.collection("Users").addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) return@addSnapshotListener
            
            fullUserList.clear()
            for (doc in snapshots) {
                val user = UserAdmin(
                    id = doc.id,
                    nameUser = doc.getString("nameUser") ?: "No Name",
                    email = doc.getString("email") ?: "",
                    status = doc.getString("status") ?: "Active"
                )
                fullUserList.add(user)
            }
            adapter.updateList(fullUserList)
        }
    }

    private fun filterUsers(query: String) {
        val filtered = fullUserList.filter { 
            it.nameUser.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
        }
        adapter.updateList(filtered)
    }

    private fun toggleBlockUser(user: UserAdmin) {
        val newStatus = if (user.status == "Active") "Blocked" else "Active"
        db.collection("Users").document(user.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Đã cập nhật trạng thái user", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteDialog(user: UserAdmin) {
        AlertDialog.Builder(this)
            .setTitle("Xóa người dùng")
            .setMessage("Bạn có chắc chắn muốn xóa ${user.nameUser}?")
            .setPositiveButton("Xóa") { _, _ ->
                db.collection("Users").document(user.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đã xóa user thành công", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}