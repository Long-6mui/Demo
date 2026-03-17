package com.example.demo.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.demo.R
import com.google.firebase.firestore.FirebaseFirestore

class EditPostActivity : AppCompatActivity() {

    lateinit var edtContent: EditText
    lateinit var imgPost: ImageView
    lateinit var btnSave: Button



    var imageRes: String = ""
    var position = -1

    val db = FirebaseFirestore.getInstance()

    //val postId = intent.getStringExtra("postId")
    var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        edtContent = findViewById(R.id.edtEditPost)
        imgPost = findViewById(R.id.imgEditPost)
        btnSave = findViewById(R.id.btnSaveEdit)

        postId = intent.getStringExtra("postId")

        val content = intent.getStringExtra("content")
        imageRes = intent.getStringExtra("image") ?: ""
        position = intent.getIntExtra("position",-1)

        edtContent.setText(content)

        if(imageRes.isNotEmpty()){
            imgPost.visibility = View.VISIBLE
            imgPost.setImageURI(Uri.parse(imageRes))
        }else{
            imgPost.visibility = View.GONE
        }

        btnSave.setOnClickListener {

            val newContent = edtContent.text.toString()

            val data = hashMapOf(
                "content" to newContent,
                "imageUrl" to imageRes
            )

            postId?.let {

                db.collection("posts")
                    .document(it)
                    .update(data as Map<String, Any>)
                    .addOnSuccessListener {

                        FeedActivity.list[position].content = newContent
                        FeedActivity.list[position].imageUrl = imageRes

                        FeedActivity.adapter.notifyItemChanged(position)

                        finish()
                    }

            }
        }
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {

        super.onActivityResult(requestCode,resultCode,data)

        if(requestCode==100 && resultCode==RESULT_OK){

            val uri = data?.data

            if(uri != null){
                imgPost.visibility = View.VISIBLE
                imgPost.setImageURI(uri)
                imageRes = uri.toString()
            }else{
                imgPost.visibility = View.GONE
            }



        }
    }
}