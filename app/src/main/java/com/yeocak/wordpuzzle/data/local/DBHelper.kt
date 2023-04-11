package com.yeocak.wordpuzzle.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.yeocak.wordpuzzle.model.Score

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DBHelper.DATABASE_NAME, null, DBHelper.DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "DB_WORDGAMEAPP"//database adı
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "Score"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_SCORE = "score"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE " + TABLE_NAME + "("
                + COL_ID + " INTEGER PRIMARY KEY," + COL_NAME + " TEXT,"
                + COL_SCORE + " INTEGER " + ")")
        //val createTable = "CREATE TABLE $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_NAME  VARCHAR(256), $COL_SCORE  INTEGER)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        //onCreate(db)
    }

    fun insertData(score: Score) {
        val sqliteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_NAME, score.name)
        contentValues.put(COL_SCORE, score.score)

        val result = sqliteDB.insert(TABLE_NAME, null, contentValues)
        if (result != -1L) {
            Log.d("melih", "record was saved successfully")
        } else {
            Log.d("melih", "Error: kayıt yapılamadı")
        }
    }

    fun readData(): List<Score> {
        val scoreList = ArrayList<Score>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME ORDER BY score DESC LIMIT 40"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val id = result.getInt(result.getColumnIndexOrThrow(COL_ID))
                val name = result.getString(result.getColumnIndexOrThrow(COL_NAME))
                val scoreValue = result.getInt(result.getColumnIndexOrThrow(COL_SCORE))
                scoreList.add(Score(id, name, scoreValue))
            } while (result.moveToNext())
        }
        result.close()
        db.close()
        return scoreList
    }

    fun deleteData() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }

}