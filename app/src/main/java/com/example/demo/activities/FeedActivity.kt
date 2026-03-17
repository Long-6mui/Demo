package com.example.demo.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.text.isNotEmpty
import com.example.demo.R
class FeedActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: PostAdapter
    lateinit var edtPost: EditText
    lateinit var btnPost: Button


    var selectedImage = 0

    val list = mutableListOf(
        Post(R.drawable.ava1, "Anna", "Hôm nay mình nấu mì Ý \uD83D\uDE1A", 3, R.drawable.miy),
        Post(R.drawable.ava2, "John", "Gà rán siêu giòn tự làm ở nhà \uD83E\uDD24", 5, R.drawable.garan),
        Post(R.drawable.ava3, "Lisa", "Tự thưởng cho mình sau 1 ngày dài \uD83D\uDE0D", 8, R.drawable.choco),
        Post(R.drawable.ava7, "MoLy", "Hôm nay ăn ngon 1 bữa \uD83D\uDE0D", 6, R.drawable.cua),
        Post(R.drawable.ava11, "Noah", "Mời cả nhà dùng cơm \uD83D\uDE0A", 4, R.drawable.moicom),
        Post(R.drawable.ava9, "Katy", "Được ảnh nấu mì cho ăn \uD83D\uDE0D", 12, R.drawable.mi),
        Post(R.drawable.ava10, "Kirin", "Thơmmm ", 7, R.drawable.tok),
        Post(R.drawable.ava6, "Airon", "Lần đầu làm chuyện đó \uD83D\uDE0A", 9, R.drawable.goicuon),
        Post(R.drawable.ava8, "Bell", "Món này quá là dễ với mình \uD83D\uDE09", 10, R.drawable.btn),
        Post(R.drawable.ava4, "Mason", "Bữa nay healthy nha \uD83D\uDE0E", 6, R.drawable.healthy),
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        recyclerView = findViewById(R.id.recyclerFeed)
        edtPost = findViewById(R.id.edtPost)
        btnPost = findViewById(R.id.btnPost)


        adapter = PostAdapter(list)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter



        btnPost.setOnClickListener {

            val content = edtPost.text.toString()

            if (content.isNotEmpty()) {

                list.add(
                    0,
                    Post(
                        imgAvatar = R.drawable.ava1,
                        name = "User",
                        content = content,
                        image = R.drawable.ava2,
                        likes = 0
                    )
                )

                adapter.notifyDataSetChanged()

                edtPost.setText("")

            }
        }
    }
}