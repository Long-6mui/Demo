package com.example.demo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ── Category clicks → CategoryActivity ──
        val categories = mapOf(
            R.id.catDryFood  to Pair("Đồ Khô",      "🍚"),
            R.id.catSoup     to Pair("Đồ Nước",     "🍜"),
            R.id.catSnack    to Pair("Ăn Vặt",      "🧆"),
            R.id.catDessert  to Pair("Tráng Miệng", "🍮"),
            R.id.catGrill    to Pair("Nướng",        "🔥"),
            R.id.catVegan    to Pair("Chay",         "🥗"),
            R.id.catDrink    to Pair("Đồ Uống",     "🧋"),
        )

        categories.forEach { (id, data) ->
            findViewById<LinearLayout>(id).setOnClickListener {
                val intent = Intent(this, CategoryActivity::class.java)
                intent.putExtra("category", data.first)
                intent.putExtra("emoji", data.second)
                startActivity(intent)
            }
        }

        // Nút "Xem thêm"
        findViewById<LinearLayout>(R.id.catMore).setOnClickListener {
            // TODO: mở trang tất cả danh mục
        }

        // Nút "Xem tất cả" ở section yêu thích
        findViewById<TextView>(R.id.txtSeeAll).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

        // ── Bottom Navigation ──
        findViewById<LinearLayout>(R.id.tabProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        val tabSaved = findViewById<LinearLayout>(R.id.tabSaved)
        findViewById<LinearLayout>(R.id.tabSaved).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
        val tabProfile = findViewById<LinearLayout>(R.id.tabProfile)

        tabProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


        val btnHome = findViewById<ImageButton>(R.id.btnHome)

        tabProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        tabSaved.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }


        btnHome.setOnClickListener {

            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)

        }
        // ── Notification button ──
        val btnNotification = findViewById<ImageButton>(R.id.btnNotification)

        btnNotification.setOnClickListener {

            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)

        }

        findViewById<LinearLayout>(R.id.catMore).setOnClickListener {
            startActivity(Intent(this, IngredientsActivity::class.java))
        }

        seedFirestore()
    }

    private fun seedFirestore() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        val categories = mapOf(
            "cat_01" to "🥩 Thịt & Hải sản",
            "cat_02" to "🥦 Rau củ",
            "cat_03" to "🧄 Gia vị",
            "cat_04" to "🌾 Gạo & Bột",
            "cat_05" to "🍄 Nấm",
            "cat_06" to "🥚 Trứng & Đậu hũ",
            "cat_07" to "🫘 Đậu & Hạt",
            "cat_08" to "🍋 Trái cây"
        )
        categories.forEach { (id, name) ->
            db.collection("category_ingre").document(id)
                .set(mapOf("cateName" to name))
        }

        val ingredients = listOf(
            Triple("ing_01", "Thịt bò",   "cat_01"),
            Triple("ing_02", "Thịt heo",  "cat_01"),
            Triple("ing_03", "Thịt gà",   "cat_01"),
            Triple("ing_04", "Tôm",       "cat_01"),
            Triple("ing_05", "Cá thu",    "cat_01"),
            Triple("ing_06", "Mực",       "cat_01"),
            Triple("ing_07", "Cua",       "cat_01"),
            Triple("ing_08", "Sườn heo",  "cat_01"),
            Triple("ing_09", "Chả lụa",   "cat_01"),
            Triple("ing_10", "Thịt vịt",  "cat_01"),
            Triple("ing_11", "Cá basa",   "cat_01"),
            Triple("ing_12", "Lòng heo",  "cat_01"),
            Triple("ing_13", "Cải xanh",  "cat_02"),
            Triple("ing_14", "Rau muống", "cat_02"),
            Triple("ing_15", "Cà rốt",    "cat_02"),
            Triple("ing_16", "Khoai tây", "cat_02"),
            Triple("ing_17", "Bí đỏ",     "cat_02"),
            Triple("ing_18", "Cà chua",   "cat_02"),
            Triple("ing_19", "Giá đỗ",    "cat_02"),
            Triple("ing_20", "Đậu bắp",   "cat_02"),
            Triple("ing_21", "Bắp cải",   "cat_02"),
            Triple("ing_22", "Rau ngót",  "cat_02"),
            Triple("ing_23", "Mướp",      "cat_02"),
            Triple("ing_24", "Khổ qua",   "cat_02"),
            Triple("ing_25", "Tỏi",       "cat_03"),
            Triple("ing_26", "Hành tím",  "cat_03"),
            Triple("ing_27", "Gừng",      "cat_03"),
            Triple("ing_28", "Sả",        "cat_03"),
            Triple("ing_29", "Ớt",        "cat_03"),
            Triple("ing_30", "Nước mắm",  "cat_03"),
            Triple("ing_31", "Tiêu",      "cat_03"),
            Triple("ing_32", "Nghệ",      "cat_03"),
            Triple("ing_33", "Hành lá",   "cat_03"),
            Triple("ing_34", "Ngò rí",    "cat_03"),
            Triple("ing_35", "Gạo tẻ",   "cat_04"),
            Triple("ing_36", "Gạo nếp",  "cat_04"),
            Triple("ing_37", "Bún",      "cat_04"),
            Triple("ing_38", "Phở",      "cat_04"),
            Triple("ing_39", "Hủ tiếu",  "cat_04"),
            Triple("ing_40", "Mì trứng", "cat_04"),
            Triple("ing_41", "Bột năng", "cat_04"),
            Triple("ing_42", "Bột mì",   "cat_04"),
            Triple("ing_43", "Nấm rơm",      "cat_05"),
            Triple("ing_44", "Nấm đông cô",  "cat_05"),
            Triple("ing_45", "Nấm kim châm", "cat_05"),
            Triple("ing_46", "Nấm bào ngư",  "cat_05"),
            Triple("ing_47", "Nấm mèo",      "cat_05"),
            Triple("ing_48", "Trứng gà",   "cat_06"),
            Triple("ing_49", "Trứng vịt",  "cat_06"),
            Triple("ing_50", "Trứng cút",  "cat_06"),
            Triple("ing_51", "Đậu hũ",     "cat_06"),
            Triple("ing_52", "Đậu hũ non", "cat_06"),
            Triple("ing_53", "Đậu xanh",   "cat_07"),
            Triple("ing_54", "Đậu đỏ",     "cat_07"),
            Triple("ing_55", "Đậu phộng",  "cat_07"),
            Triple("ing_56", "Hạt sen",    "cat_07"),
            Triple("ing_57", "Đậu đen",    "cat_07"),
            Triple("ing_58", "Đậu hà lan", "cat_07"),
            Triple("ing_59", "Chanh", "cat_08"),
            Triple("ing_60", "Dứa",   "cat_08"),
            Triple("ing_61", "Chuối", "cat_08"),
            Triple("ing_62", "Xoài",  "cat_08"),
            Triple("ing_63", "Đu đủ", "cat_08"),
            Triple("ing_64", "Dừa",   "cat_08"),
            Triple("ing_65", "Mít",   "cat_08"),
        )

        ingredients.forEach { (id, name, catId) ->
            db.collection("ingredients").document(id)
                .set(mapOf(
                    "nameIngre" to name,
                    "cateinID"  to catId,
                    "imgUrl"    to ""
                ))
        }

        android.util.Log.d("SEED", "✅ Seed xong!")
        android.widget.Toast.makeText(this, "✅ Seed Firebase xong!", android.widget.Toast.LENGTH_SHORT).show()
    }
}