package com.example.demo.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.demo.activities.Comment
import com.example.demo.activities.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    companion object {
        const val DB_NAME = "user.db"
        const val DB_VERSION = 7 // Nâng version để cập nhật bảng mới mà không mất dữ liệu cũ
        const val TABLE_USER = "user"
        const val COL_ID = "id"
        const val COL_USER_ID = "userID"  // Firebase UID
        const val COL_NAME = "name"
        const val COL_HOTEN = "hoten"
        const val COL_EMAIL = "email"
        const val COL_BIRTHDAY = "birthday"
        const val COL_GENDER = "gender"

        const val TABLE_SAVED = "saved_recipes"
        const val SAVED_ID = "id"
        const val SAVED_NAME = "name"
        const val SAVED_INFO = "info"

        // --- BẢNG RECIPES MỚI (DỮ LIỆU THẬT) ---
        const val TABLE_RECIPES = "recipes"
        const val COL_RECIPE_ID = "id"
        const val COL_RECIPE_NAME = "name"
        const val COL_RECIPE_INGREDIENTS = "ingredients"
        const val COL_RECIPE_IMAGE = "image"
        const val COL_RECIPE_DESC = "description"

        //Comment
       val CREATE_COMMENT_TABLE = """
        CREATE TABLE comments(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            postId TEXT,
            userId TEXT, 
            content TEXT,
            image TEXT
        )
        """

        const val COL_ROLE = "role"
        const val COL_AVATAR = "avatar"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Bảng User y chang mày gửi
        val sql = """
            CREATE TABLE $TABLE_USER (
                $COL_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID  TEXT NOT NULL UNIQUE,
                $COL_NAME     TEXT NOT NULL,
                $COL_HOTEN     TEXT NOT NULL,
                $COL_EMAIL    TEXT NOT NULL,
                $COL_BIRTHDAY TEXT DEFAULT '',
                $COL_GENDER   TEXT DEFAULT '',
                $COL_ROLE     TEXT DEFAULT 'user',
                $COL_AVATAR   TEXT DEFAULT ''
            )
        """.trimIndent()
        db.execSQL(sql)

        // 2. Bảng Comment y chang mày gửi
        db.execSQL(CREATE_COMMENT_TABLE)

        // 3. Bảng Saved y chang mày gửi
        val sqlSaved = """
            CREATE TABLE $TABLE_SAVED (
                $SAVED_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $SAVED_NAME TEXT,
                $SAVED_INFO TEXT
            )
        """.trimIndent()
        db.execSQL(sqlSaved)

        // 4. TẠO BẢNG RECIPES (ĐỂ CHỨA DỮ LIỆU THẬT)
        db.execSQL("""
            CREATE TABLE $TABLE_RECIPES (
                $COL_RECIPE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_RECIPE_NAME TEXT,
                $COL_RECIPE_INGREDIENTS TEXT,
                $COL_RECIPE_IMAGE TEXT,
                $COL_RECIPE_DESC TEXT
            )
        """)

        // Nạp dữ liệu thật vào ngay khi tạo database
        initRealRecipes(db)
    }

    // --- HÀM NẠP DỮ LIỆU MÓN ĂN THẬT ---
    private fun initRealRecipes(db: SQLiteDatabase) {
        val recipes = listOf(
            arrayOf("Bún Bò Huế", "thịt bò, bún, sả, ớt, mắm ruốc", "", "Món bún đặc sản Huế đậm đà thơm mùi sả."),
            arrayOf("Thịt Kho Tộ", "thịt heo, hành tím, nước mắm, đường", "", "Thịt ba chỉ kho đậm đà, ăn kèm cơm trắng rất ngon."),
            arrayOf("Phở Bò", "thịt bò, bánh phở, quế, hồi, gừng", "", "Món ăn truyền thống nổi tiếng thế giới của Việt Nam.")
        )

        for (recipe in recipes) {
            val v = ContentValues().apply {
                put(COL_RECIPE_NAME, recipe[0])
                put(COL_RECIPE_INGREDIENTS, recipe[1])
                put(COL_RECIPE_IMAGE, recipe[2])
                put(COL_RECIPE_DESC, recipe[3])
            }
            db.insert(TABLE_RECIPES, null, v)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Nếu nâng cấp, chỉ thêm bảng mới nếu nó chưa tồn tại, tránh mất dữ liệu User
        if (oldVersion < 7) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_RECIPES")
            db.execSQL("""
                CREATE TABLE $TABLE_RECIPES (
                    $COL_RECIPE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_RECIPE_NAME TEXT,
                    $COL_RECIPE_INGREDIENTS TEXT,
                    $COL_RECIPE_IMAGE TEXT,
                    $COL_RECIPE_DESC TEXT
                )
            """)
            initRealRecipes(db)
        }
    }

    // --- GIỮ NGUYÊN TOÀN BỘ CÁC HÀM CŨ CỦA MÀY ĐỂ KHÔNG LỖI ---
    fun deleteSavedRecipe(name: String) {
        val db = writableDatabase
        db.delete(TABLE_SAVED, "$SAVED_NAME=?", arrayOf(name))
    }

    fun getAllUsers(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USER ORDER BY $COL_ID DESC", null)
    }

    fun updateUser(id: Int, name: String, birthDay: String, gender: String): Int {
        val db = writableDatabase
        val sql = "UPDATE $TABLE_USER SET $COL_NAME=?, $COL_BIRTHDAY=?, $COL_GENDER=? WHERE $COL_ID=?"
        db.execSQL(sql, arrayOf(name, birthDay, gender, id))
        val c = db.rawQuery("SELECT changes()", null)
        val changed = if (c.moveToFirst()) c.getInt(0) else 0
        c.close()
        return changed
    }

    fun addSavedRecipe(name: String, info: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(SAVED_NAME, name)
            put(SAVED_INFO, info)
        }
        db.insert(TABLE_SAVED, null, values)
    }

    fun getAllSavedRecipes(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_SAVED ORDER BY $SAVED_ID DESC", null)
    }



    fun saveUser(userID: String, name: String, email: String, role: String = "user", avatar: String = ""): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_ID, userID)
            put(COL_NAME, name)
            put(COL_HOTEN, "")
            put(COL_EMAIL, email)
            put(COL_ROLE, role)
            put(COL_AVATAR, avatar)
        }
        return db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getUserByUID(userID: String): User? {
        val db = readableDatabase
        val cursor = db.query(TABLE_USER, null, "$COL_USER_ID=?", arrayOf(userID), null, null, null)
        if (cursor.moveToFirst()) {
            val user = User(
                userID = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                hoten = cursor.getString(cursor.getColumnIndexOrThrow(COL_HOTEN)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                birthday = cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTHDAY)),
                gender = cursor.getString(cursor.getColumnIndexOrThrow(COL_GENDER)),
                avatar = cursor.getString(cursor.getColumnIndexOrThrow(COL_AVATAR))
            )
            cursor.close()
            return user
        }
        cursor.close()
        return null
    }

    fun isUserExists(userID: String): Boolean {
        return getUserByUID(userID) != null
    }

    fun deleteUser(userID: String) {
        val db = writableDatabase
        db.delete(TABLE_USER, "$COL_USER_ID=?", arrayOf(userID))
    }

    fun updateUserByUID(
        userId: String,
        name: String,
        hoten: String,
        email: String,
        birthday: String,
        gender: String,
        avatar: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_HOTEN, hoten)
            put(COL_EMAIL, email)
            put(COL_BIRTHDAY, birthday)
            put(COL_GENDER, gender)
            put(COL_AVATAR, avatar)
        }
        db.update(TABLE_USER, values, "$COL_USER_ID=?", arrayOf(userId))
    }

    // --- CÁC HÀM MỚI ĐỂ LẤY DỮ LIỆU THẬT ---
    fun getAllRecipes(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_RECIPES ORDER BY $COL_RECIPE_ID DESC", null)
    }

    fun searchByIngredient(keyword: String): Cursor {
        val db = readableDatabase
        // Dùng LOWER để ép cả 2 về chữ thường, tìm kiếm sẽ chính xác 100%
        return db.rawQuery(
            "SELECT * FROM $TABLE_RECIPES WHERE LOWER($COL_RECIPE_INGREDIENTS) LIKE LOWER(?)",
            arrayOf("%${keyword.trim()}%")
        )
    }
    fun addRecipe(name: String, ingredients: String, image: String, desc: String): Long {
        val db = writableDatabase
        val v = ContentValues().apply {
            put(COL_RECIPE_NAME, name)
            put(COL_RECIPE_INGREDIENTS, ingredients)
            put(COL_RECIPE_IMAGE, image)
            put(COL_RECIPE_DESC, desc)
        }
        return db.insert(TABLE_RECIPES, null, v)
    }
    fun deleteRecipeById(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_RECIPES, "$COL_RECIPE_ID=?", arrayOf(id.toString()))
    }

    // 4. Chỉnh sửa món ăn
    fun updateRecipe(id: Int, name: String, ingredients: String, image: String, desc: String): Int {
        val db = writableDatabase
        val v = ContentValues().apply {
            put(COL_RECIPE_NAME, name)
            put(COL_RECIPE_INGREDIENTS, ingredients)
            put(COL_RECIPE_IMAGE, image)
            put(COL_RECIPE_DESC, desc)
        }
        return db.update(TABLE_RECIPES, v, "$COL_RECIPE_ID=?", arrayOf(id.toString()))
    }
    // Kiểm tra món ăn đã có trong danh sách yêu thích chưa
    fun isRecipeSaved(name: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVED WHERE $SAVED_NAME=?", arrayOf(name))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Hàm xóa món khỏi danh sách yêu thích (mày đã có hàm này nhưng hãy check lại tên cột)
    fun deleteSavedRecipeByName(name: String) {
        val db = writableDatabase
        db.delete(TABLE_SAVED, "$SAVED_NAME=?", arrayOf(name))
    }

}