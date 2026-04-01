package com.example.demo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.demo.R
import com.example.demo.Database.DatabaseHelper
import com.example.demo.models.Dish

class SavedAdapter(
    private val dishList: MutableList<Dish>,
    private val isSavedScreen: Boolean = false // Nếu true là màn hình Saved, false là màn hình Suggest
) : RecyclerView.Adapter<SavedAdapter.DishViewHolder>() {

    class DishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDish: ImageView = view.findViewById(R.id.imgRecipe)
        val tvName: TextView = view.findViewById(R.id.tvRecipeName)
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)
        val btnAction: ImageButton = view.findViewById(R.id.btnAction) // Đổi ID này cho dùng chung
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_recipe, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishList[position]
        val context = holder.itemView.context
        val dbHelper = DatabaseHelper(context)

        holder.tvName.text = dish.name
        holder.tvInfo.text = dish.info
        holder.imgDish.setImageResource(dish.imgRes)

        // --- XỬ LÝ LOGIC THEO MÀN HÌNH ---
        if (isSavedScreen) {
            // Màn hình Yêu Thích: Hiện nút Xóa (Thùng rác hoặc dấu X)
            holder.btnAction.setImageResource(R.drawable.ic_delete) // Mày thêm icon delete vào drawable nhé
            holder.btnAction.setOnClickListener {
                dbHelper.deleteSavedRecipe(dish.name) // Xóa trong bảng saved_recipes
                dishList.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                Toast.makeText(context, "Đã xóa khỏi yêu thích!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Màn hình Gợi Ý: Hiện nút Trái Tim
            val isAlreadySaved = dbHelper.isRecipeSaved(dish.name)

            // Set icon ban đầu tùy theo đã lưu hay chưa
            holder.btnAction.setImageResource(
                if (isAlreadySaved) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )

            holder.btnAction.setOnClickListener {
                val currentlySaved = dbHelper.isRecipeSaved(dish.name)
                if (!currentlySaved) {
                    dbHelper.addSavedRecipe(dish.name, dish.info)
                    holder.btnAction.setImageResource(R.drawable.ic_heart_filled)
                    Toast.makeText(context, "Đã lưu!", Toast.LENGTH_SHORT).show()
                } else {
                    dbHelper.deleteSavedRecipe(dish.name)
                    holder.btnAction.setImageResource(R.drawable.ic_heart_outline)
                    Toast.makeText(context, "Đã bỏ lưu!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = dishList.size
}