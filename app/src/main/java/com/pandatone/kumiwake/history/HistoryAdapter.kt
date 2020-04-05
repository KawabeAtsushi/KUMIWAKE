package com.pandatone.kumiwake.history

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.ArrayAdapter
import com.pandatone.kumiwake.member.function.Group
import java.sql.Timestamp

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class HistoryAdapter(context: Context) : ArrayAdapter<Group>(context, 0) {

    private var dbHelper: DatabaseHelper

    val getDB: Cursor
        get() = db.query(TABLE_NAME, null, null, null, null, null, null)

    val maxId: Int
        @SuppressLint("Recycle")
        get() {
            open()
            val c = db.rawQuery("SELECT MAX($HS_ID) FROM $TABLE_NAME", null)
            c.moveToFirst()
            val idMax = c.getInt(0)
            close()
            return idMax
        }

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

    //すべてのグループ取得
    fun getAllGroups(): ArrayList<Group> {
        val groupListList: ArrayList<Group> = ArrayList()
        open()
        //getCursor(getDB, groupListList)
        close()
        return groupListList
    }

    //Listの情報取得
    fun getCursor(c: Cursor, historyList: ArrayList<History>) {
        var history: History

        historyList.clear()

        if (c.moveToFirst()) {
            do {
                history = History(
                        c.getInt(c.getColumnIndex(HS_ID)),
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

    //再組み分けしたときの状態を更新
    fun changeHistory(id: Int, result: String) {
        val millis = System.currentTimeMillis()
        val timestamp = Timestamp(millis)
        open()
        val values = ContentValues()
        values.put(HS_NAME, timestamp.toString())
        values.put(HS_RESULT, result)
        try {
            db.update(TABLE_NAME, values, "$HS_ID=?", arrayOf(id.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        close()
    }

    companion object {
        const val DB_VERSION = 1
        const val TABLE_NAME = "history_table"
        const val HS_ID = "_id"
        const val HS_NAME = "hs_name"
        const val HS_RESULT = "hs_result"
        const val HS_MODE = "hs_mode"
        const val HS_KEEP = "hs_keep"
        const val HS_PARENT = "hs_parent"
        lateinit var db: SQLiteDatabase

        const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " ("
                + HS_ID + " INTEGER PRIMARY KEY," + HS_NAME + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + HS_RESULT + " TEXT," + HS_MODE + " INTEGER," + HS_KEEP + " INTEGER DEFAULT -1,"
                + HS_PARENT + " INTEGER DEFAULT -1" + ");")
    }
}
