package com.example.demo.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.text.isNotEmpty
import com.example.demo.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class FeedActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView

    lateinit var edtPost: EditText
    lateinit var btnPost: Button

    lateinit var imgPick: ImageView

    companion object {
        var list = mutableListOf<Post>()
        lateinit var adapter: PostAdapter
    }


    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var imageUri: Uri? = null


    val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
            imgPick.setImageURI(uri)
        }



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        recyclerView = findViewById(R.id.recyclerFeed)
        edtPost = findViewById(R.id.edtPost)
        btnPost = findViewById(R.id.btnPost)
        imgPick = findViewById(R.id.imgPick)

        //Post mẫu
        if(list.isEmpty()){
            list.add(
                Post(
                    "",
                    R.drawable.ava1,
                    "Anna",
                    "Hôm nay mình nấu mì Ý \uD83D\uDE1A",
                    3,
                    "https://daubepgiadinh.vn/wp-content/uploads/2018/05/hinh-mi-y-ngon.jpg"
                )
            )

            list.add(
                Post(
                    "",
                    R.drawable.ava2,
                    "John",
                    "Gà rán siêu giòn tự làm ở nhà \uD83E\uDD24",
                    5,
                    "https://cdn.eva.vn//upload/3-2016/images/2016-07-22/uc-ga-kfc-mon-ngon-be-thich-uc-ga-kfc--6--1469194272-width500height375.jpg"
                )
            )

            list.add(
                Post(
                    "",
                    R.drawable.ava3,
                    "Lisa",
                    "Tự thưởng cho mình sau 1 ngày dài \uD83D\uDE0D",
                    8,
                    "https://images2.thanhnien.vn/528068263637045248/2023/2/14/6-pan-cake-16763820419171669639791.jpg"
                )
            )
        }



        adapter = PostAdapter(list)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadPosts()


        imgPick.setOnClickListener {
            pickImage.launch("image/*")
        }


        btnPost.setOnClickListener {

            val content = edtPost.text.toString()

            if (content.isNotEmpty()) {

                if(imageUri != null){
                    uploadImage(content)
                }else{
                    savePost(content,"")
                }

                edtPost.setText("")
            }
        }
        val btnBack = findViewById<ImageView>(R.id.btnBackHome)

        btnBack.setOnClickListener {

                finish()


        }
    }


    fun uploadImage(content: String) {

        val ref = storage.reference.child("posts/" + UUID.randomUUID())

        imageUri?.let {

            ref.putFile(it)
                .addOnSuccessListener {

                    ref.downloadUrl.addOnSuccessListener { url ->

                        savePost(content, url.toString())

                    }

                }

        }
    }

    fun savePost(content: String, imageUrl: String){

        val post = Post(
            imgAvatar = R.drawable.ava5,
            name = "User",
            content = content,
            likes = 0,
            imageUrl = imageUrl
        )

        list.add(0, post)
        adapter.notifyDataSetChanged()

        val data = hashMapOf(
            "name" to "User",
            "content" to content,
            "likes" to 0,
            "imageUrl" to imageUrl
        )

        db.collection("posts")
            .add(data)
            .addOnSuccessListener { document ->

                post.id = document.id   // lưu id Firebase vào post

            }
    }

    fun loadPosts(){

        db.collection("posts")
            .get()
            .addOnSuccessListener { result ->

                if(result.isEmpty){
                    return@addOnSuccessListener
                }

                list.clear()

                for (doc in result) {

                    val post = doc.toObject(Post::class.java)
                    post.id = doc.id

                    list.add(post)
                }

                adapter.notifyDataSetChanged()
            }
    }
}