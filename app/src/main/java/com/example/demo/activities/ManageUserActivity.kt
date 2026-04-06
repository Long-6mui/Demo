package com.example.demo.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class ManageUserActivity : AppCompatActivity() {

    private lateinit var adapter: UserAdapter
    private val fullUserList = mutableListOf<UserAdmin>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var edtSearchUser: EditText
    
    //Handler để tự động refresh giao diện nhạy hơn (30 giây)
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (::adapter.isInitialized) {
                // Chỉ làm mới các item đang hiển thị để tính toán lại isActuallyOnline
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
            }
            refreshHandler.postDelayed(this, 30000) // Lặp lại sau 30 giây
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_user)

        val recyclerUsers = findViewById<RecyclerView>(R.id.recyclerUsers)
        edtSearchUser = findViewById(R.id.edtSearchUser)
        val btnBack = findViewById<ImageButton>(R.id.btnBackManageUser)

        btnBack.setOnClickListener { finish() }

        recyclerUsers.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(
            mutableListOf(),
            onBlockClick = { user, _ -> toggleBlockUser(user) },
            onDeleteClick = { user, _ -> showDeleteDialog(user) }
        )
        recyclerUsers.adapter = adapter

        loadUsersFromFirestore()

        edtSearchUser.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterUsers(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        refreshHandler.post(refreshRunnable) 
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable) 
    }

    private fun loadUsersFromFirestore() {
        db.collection("Users").addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) return@addSnapshotListener
            
            fullUserList.clear()
            for (doc in snapshots) {
                val role = doc.getString("role") ?: "user"
                if (role == "admin") continue
                
                val user = UserAdmin(
                    id = doc.id,
                    nameUser = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    avatar = doc.getString("avatar") ?: "",
                    status = doc.getString("status") ?: "Active",
                    isOnline = doc.getBoolean("isOnline") ?: false,
                    lastSeen = doc.getTimestamp("lastSeen")
                )
                fullUserList.add(user)
            }
            filterUsers(edtSearchUser.text.toString())
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
        db.collection("Users").document(user.id).update("status", newStatus)
            .addOnSuccessListener { Toast.makeText(this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show() }
    }

    private fun showDeleteDialog(user: UserAdmin) {
        AlertDialog.Builder(this)
            .setTitle("Xóa người dùng")
            .setMessage("Bạn có chắc chắn muốn xóa ${user.nameUser}?")
            .setPositiveButton("Xóa") { _, _ ->
                db.collection("Users").document(user.id).delete()
                    .addOnSuccessListener { Toast.makeText(this, "Đã xóa user thành công", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}