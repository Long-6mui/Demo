package com.example.demo.activities

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.Database.DatabaseHelper
import com.example.demo.R

class CommentActivity : AppCompatActivity() {

    lateinit var edtComment: EditText
    lateinit var btnSend: ImageView
    lateinit var btnPickImage: ImageView

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: CommentAdapter
    var list = mutableListOf<Comment>()

    var imageUri: Uri? = null

    lateinit var db: DatabaseHelper
    var postId: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)


        db = DatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerComment)

        adapter = CommentAdapter(list, db) {
            loadComments()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        edtComment = findViewById(R.id.edtComment)
        btnSend = findViewById(R.id.btnSend)
        btnPickImage = findViewById(R.id.btnPickImage)



        postId = intent.getStringExtra("postId") ?: ""

        loadComments()

        val back = findViewById<ImageView>(R.id.btnBack)
        back.setOnClickListener { finish() }

        val pickImage =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                imageUri = uri
                btnPickImage.setImageURI(uri)
            }

        btnPickImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSend.setOnClickListener {

            val content = edtComment.text.toString()

            val comment = Comment(
                postId = postId,
                user = "User",
                content = content,
                image = imageUri?.toString() ?: ""
            )

            db.addComment(comment)

            list.add(comment)
            adapter.notifyItemInserted(list.size - 1)

            edtComment.setText("")
        }
    }

    fun loadComments(){

        list.clear()

        val comments = db.getComments(postId)

        list.addAll(comments)

        adapter.notifyDataSetChanged()
    }
}