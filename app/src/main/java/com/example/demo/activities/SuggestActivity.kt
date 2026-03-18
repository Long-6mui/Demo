package com.example.demo.activities

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.models.Dish


class SuggestActivity : AppCompatActivity() {

    private lateinit var rvSuggestions: RecyclerView
    private lateinit var btnSuggest: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Đảm bảo bạn đã copy file activity_suggest.xml mới mà tôi gửi trước đó
        setContentView(R.layout.activity_suggest)

        // 1. Ánh xạ View
        rvSuggestions = findViewById(R.id.rvSuggestions)
        btnSuggest = findViewById(R.id.btnSuggest)

        // 2. Thiết lập LayoutManager
        rvSuggestions.layoutManager = LinearLayoutManager(this)

        // 3. Xử lý sự kiện Click
        btnSuggest.setOnClickListener {
            // Lấy dữ liệu (hiện tại là giả lập)
            val suggestedList = getSuggestionsFromDb()

            // Đổ dữ liệu vào Adapter
            val adapter = SavedAdapter(suggestedList)
            rvSuggestions.adapter = adapter
        }
    }

    /**
     * Hàm giả lập lấy dữ liệu.
     * Sau này bạn sẽ thay thế bằng dbHelper.getRecipesByIngredients(...)
     */
    private fun getSuggestionsFromDb(): List<Dish> {
        return listOf(
            Dish(1, "Pasta Carbonara", R.drawable.mi, "30 phút • Dễ làm"),
            Dish(2, "Phở Bò", R.drawable.pho, "60 phút • Trung bình"),
            Dish(3, "Bún Bò Huế", R.drawable.bunbo, "45 phút • Khó"),
            Dish(4, "Gỏi Cuốn", R.drawable.goicuon, "20 phút • Dễ làm")
        )
    }
}