package com.example.demo.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    companion object {
        const val DB_NAME = "user.db"
        const val DB_VERSION = 2
        const val TABLE_USER = "user"
        const val COL_ID = "id"

        const val COL_USER_ID = "userID"  // Firebase UID
        const val COL_NAME = "name"

        const val COL_EMAIL = "email"
        const val COL_BIRTHDAY = "birthday"
        const val COL_GENDER = "gender"

        const val COL_ROLE = "role"

        const val COL_AVATAR = "avatar"
    }
    override fun onCreate(db: SQLiteDatabase) {
        val sql = """
            CREATE TABLE $TABLE_USER (
                $COL_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID  TEXT NOT NULL UNIQUE,
                $COL_NAME     TEXT NOT NULL,
                $COL_EMAIL    TEXT NOT NULL,
                $COL_BIRTHDAY TEXT DEFAULT '',
                $COL_GENDER   TEXT DEFAULT '',
                $COL_ROLE     TEXT DEFAULT 'user',
                $COL_AVATAR   TEXT DEFAULT ''
            )
        """.trimIndent()
        db.execSQL(sql)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion:
    Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
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

    // Lưu user sau khi đăng nhập Google
    fun saveUser(userID: String, name: String, email: String,
                 role: String = "user", avatar: String = ""): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_ID, userID)
            put(COL_NAME, name)
            put(COL_EMAIL, email)
            put(COL_ROLE, role)
            put(COL_AVATAR, avatar)
        }
        // insertWithOnConflict: nếu userID đã tồn tại → cập nhật, không tạo mới
        return db.insertWithOnConflict(TABLE_USER, null, values,
            SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Lấy thông tin user theo Firebase UID
    fun getUserByUID(userID: String): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER, null,
            "$COL_USER_ID=?", arrayOf(userID),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            mapOf(
                "id"       to cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)),
                "userID"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                "name"     to cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                "email"    to cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                "birthday" to cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTHDAY)),
                "gender"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_GENDER)),
                "role"     to cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE)),
                "avatar"   to cursor.getString(cursor.getColumnIndexOrThrow(COL_AVATAR))
            ).also { cursor.close() }
        } else {
            cursor.close()
            null
        }
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



}