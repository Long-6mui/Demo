package com.example.demo.Database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    companion object {
        const val DB_NAME = "user.db"
        const val DB_VERSION = 1
        const val TABLE_USER = "user"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_BIRTHDAY = "birthday"
        const val COL_GENDER = "gender"
    }
    override fun onCreate(db: SQLiteDatabase) {
        val sql = """CREATE TABLE $TABLE_USER ( 
        $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
        $COL_NAME TEXT NOT NULL, 
        $COL_BIRTHDAY DATE NOT NULL,
        $COL_GENDER TINYINT NOT NULL
        )"""
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
}