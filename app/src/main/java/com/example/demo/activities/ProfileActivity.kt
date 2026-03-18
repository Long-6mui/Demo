package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demo.R
import android.widget.LinearLayout
import android.widget.ImageView
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.Recipe
import com.example.demo.adapters.RecipeAdapter

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val editProfile = findViewById<LinearLayout>(R.id.menuEditProfile)
        val menuFeedback = findViewById<LinearLayout>(R.id.menuFeedback)
        val menuCategory = findViewById<LinearLayout>(R.id.menuCategory)
        val menuContent = findViewById<LinearLayout>(R.id.menuAccountContent)
        val iconArrow = findViewById<ImageView>(R.id.iconArrow)

        btnBack.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            //Đăng xuất Firebase trước
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

            // Sau đó mới chuyển màn
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        editProfile.setOnClickListener {
            startActivity(Intent(this, editInfoActivity::class.java))
        }
        menuFeedback.setOnClickListener {

            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)

        }


        menuCategory.setOnClickListener {

            if (menuContent.visibility == View.GONE) {
                menuContent.visibility = View.VISIBLE
                iconArrow.rotation = 180f
            } else {
                menuContent.visibility = View.GONE
                iconArrow.rotation = 0f
            }

        }
        val menuSetting = findViewById<LinearLayout>(R.id.menuSetting)

        menuSetting.setOnClickListener {

            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)

        }

        val recyclerMyPosts = findViewById<RecyclerView>(R.id.recyclerMyPosts)

        recyclerMyPosts.isNestedScrollingEnabled = false

        val list = listOf(
            Recipe(R.drawable.goicuon, "Gỏi Cuốn Miền Tây", "Trái Banh"),
            Recipe(R.drawable.garan, "Gà Rán", "Trái Banh"),
            Recipe(R.drawable.cua, "Cua Rang Me", "Trái Banh")
        )

        recyclerMyPosts.layoutManager =
            GridLayoutManager(this, 2)

        recyclerMyPosts.adapter = RecipeAdapter(list)
    }
}