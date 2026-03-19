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
        const val DB_VERSION = 3
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

        //Comment
        val CREATE_COMMENT_TABLE = """
        CREATE TABLE comments(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            postId TEXT,
            user TEXT,
            content TEXT,
            image TEXT
        )
        """

        const val COL_ROLE = "role"

        const val COL_AVATAR = "avatar"
    }
    override fun onCreate(db: SQLiteDatabase) {
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

        //Comment
        db.execSQL(CREATE_COMMENT_TABLE)
        val sqlSaved = """
    CREATE TABLE $TABLE_SAVED (
        $SAVED_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $SAVED_NAME TEXT,
        $SAVED_INFO TEXT
    )
""".trimIndent()
        db.execSQL(sqlSaved)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db.execSQL("DROP TABLE IF EXISTS comments")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVED") // Phải thêm dòng này để không bị crash
        onCreate(db)
    }

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
        val values = android.content.ContentValues().apply {
            put(SAVED_NAME, name)
            put(SAVED_INFO, info)
        }
        db.insert(TABLE_SAVED, null, values)
    }

    // --- Hàm lấy toàn bộ danh sách món đã lưu ---
    fun getAllSavedRecipes(): android.database.Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_SAVED ORDER BY $SAVED_ID DESC", null)
    }



    fun addComment(comment: Comment){

        val db = writableDatabase

        val values = ContentValues()
        values.put("postId", comment.postId)
        values.put("userId", comment.userId)
        values.put("content", comment.content)
        values.put("image", comment.image)

        db.insert("comments", null, values)

    }

    fun getComments(postId: String): MutableList<Comment>{

        val list = mutableListOf<Comment>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM comments WHERE postId=? ORDER BY id DESC",
            arrayOf(postId)
        )

        if(cursor.moveToFirst()){
            do{
                val c = Comment(
                    id = cursor.getInt(0),
                    postId = cursor.getString(1),
                    userId = cursor.getString(2),
                    content = cursor.getString(3),
                    image = cursor.getString(4)
                )
                list.add(c)

            }while(cursor.moveToNext())
        }

        cursor.close()

        return list
    }

    fun deleteComment(id:Int){

        val db = writableDatabase

        db.delete(
            "comments",
            "id=?",
            arrayOf(id.toString())
        )

    }

    fun updateComment(comment: Comment){

        val db = writableDatabase

        val values = ContentValues()
        values.put("content", comment.content)
        values.put("image", comment.image)

        db.update(
            "comments",
            values,
            "id=?",
            arrayOf(comment.id.toString())
        )

    }

    // Lưu user sau khi đăng nhập Google
    fun saveUser(userID: String, name: String, email: String,
                 role: String = "user", avatar: String = ""): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_ID, userID)
            put(COL_NAME, name)
            put(COL_HOTEN, "")
            put(COL_EMAIL, email)
            put(COL_ROLE, role)
            put(COL_AVATAR, avatar)
        }
        // insertWithOnConflict: nếu userID đã tồn tại → cập nhật, không tạo mới
        return db.insertWithOnConflict(TABLE_USER, null, values,
            SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Lấy thông tin user theo Firebase UID
    fun getUserByUID(userID: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            null,
            "$COL_USER_ID=?",
            arrayOf(userID),
            null,
            null,
            null
        )
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

    // Kiểm tra đã có user chưa
    fun isUserExists(userID: String): Boolean {
        return getUserByUID(userID) != null
    }

    // ✅ Xóa user khi logout (optional)
    fun deleteUser(userID: String) {
        val db = writableDatabase
        db.delete(TABLE_USER, "$COL_USER_ID=?", arrayOf(userID))
    }

    //cập nhật SQLite sau khi update Firebase
    fun updateUserByUID(
        userID: String,
        name: String,
        hoten: String,
        birthday: String,
        gender: String,
        avatar: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_HOTEN, hoten)
            put(COL_BIRTHDAY, birthday)
            put(COL_GENDER, gender)
            put(COL_AVATAR, avatar)
        }
        db.update(TABLE_USER, values, "$COL_USER_ID=?", arrayOf(userID)
        )
    }

}