package com.pandatone.kumiwake.history

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.ArrayAdapter

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class HistoryAdapter(context: Context) : ArrayAdapter<History>(context, 0) {

    private var dbHelper: DatabaseHelper

    val getDB: Cursor
        get() = db.query(TABLE_NAME, null, null, null, null, null, null)

    init {
        dbHelper = DatabaseHelper(this.context)
    }


    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, TABLE_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        }
    }

    fun open(): HistoryAdapter {
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        dbHelper.close()
    }

    private val maxId: Int
        @SuppressLint("Recycle")
        get() {
            val c = db.rawQuery("SELECT MAX($HS_ID) FROM $TABLE_NAME", null)
            c.moveToFirst()
            return c.getInt(0)
        }

    //すべての履歴取得
    fun getAllHistories(): ArrayList<History> {
        val historyList: ArrayList<History> = ArrayList()
        open()
        //getCursor(getDB, groupListList)
        close()
        return historyList
    }

    //Listの情報取得
    fun getCursor(c: Cursor, historyList: ArrayList<History>) {
        var history: History

        historyList.clear()

        if (c.moveToFirst()) {
            do {
                history = History(
                        c.getInt(0),
                        c.getString(c.getColumnIndex(HS_TIME)),
                        c.getString(c.getColumnIndex(HS_NAME)),
                        c.getString(c.getColumnIndex(HS_RESULT)),
                        c.getInt(c.getColumnIndex(HS_MODE)),
                        c.getInt(c.getColumnIndex(HS_KEEP)),
                        c.getInt(c.getColumnIndex(HS_PARENT))
                )
                historyList.add(history)          // 取得した要素をgroupListに追加

            } while (c.moveToNext())
        }
        c.close()
    }

    fun saveHistory(result: String, mode: Int, parent: Int) {
        open()
        db.beginTransaction()          // トランザクション開始
        try {
            val values = ContentValues()
            values.put(HS_RESULT, result)
            values.put(HS_MODE, mode)
            values.put(HS_PARENT, parent)
            db.insert(TABLE_NAME, null, values)
            db.setTransactionSuccessful()      // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()                // トランザクションの終了
        }
        close()
    }

    //履歴画面での操作を反映
    fun updateHistoryState(id: Int, name: String, keep: Boolean) {
        var keepInt = -1
        if (keep) {
            keepInt = 1
        }
        open()
        val values = ContentValues()
        values.put(HS_NAME, name)
        values.put(HS_KEEP, keepInt)
        try {
            db.update(TABLE_NAME, values, "$HS_ID=?", arrayOf(id.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        close()
    }

    //再組み分けしたときに結果を更新する（古いの消して新しいの追加）
    fun changeHistory(result: String, mode: Int, parent: Int) {
        open()
        db.beginTransaction()                      // トランザクション開始
        try {
            db.delete(TABLE_NAME, "$HS_ID=?", arrayOf(maxId.toString()))
            db.setTransactionSuccessful()          // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()                    // トランザクションの終了
        }
        close()
        saveHistory(result,mode,parent)
    }

    companion object {
        const val DB_VERSION = 1
        const val TABLE_NAME = "history_table"
        const val HS_ID = "_id"
        const val HS_TIME = "hs_time"
        const val HS_NAME = "hs_name"
        const val HS_RESULT = "hs_result"
        const val HS_MODE = "hs_mode"
        const val HS_KEEP = "hs_keep"
        const val HS_PARENT = "hs_parent"
        lateinit var db: SQLiteDatabase

        const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " ("
                + HS_ID + " INTEGER PRIMARY KEY,"
                + HS_TIME + " TIMESTAMP DEFAULT (DATETIME('now','localtime')),"
                + HS_NAME + " TEXT NOT NULL DEFAULT (DATETIME('now','localtime')),"
                + HS_RESULT + " TEXT," + HS_MODE + " INTEGER," + HS_KEEP + " INTEGER DEFAULT -1,"
                + HS_PARENT + " INTEGER DEFAULT -1" + ");")
    }
}
