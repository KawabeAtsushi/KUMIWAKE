package com.pandatone.kumiwake.adapter

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.ArrayAdapter
import com.pandatone.kumiwake.member.function.Group
import java.io.IOException

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class GroupAdapter(context: Context) : ArrayAdapter<Group>(context, 0) {

    private var dbHelper: DatabaseHelper

    val getDB: Cursor
        get() = db.query(TABLE_NAME, null, null, null, null, null, null)

    val maxId: Int
        @SuppressLint("Recycle")
        get() {
            open()
            val c = db.rawQuery("SELECT MAX($GP_ID) FROM $TABLE_NAME", null)
            c.moveToFirst()
            val idMax = c.getInt(0)
            close()
            return idMax
        }

    init {
        dbHelper = DatabaseHelper(this.context)
    }


    private class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, TABLE_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL(
                    "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + GP_READ
                            + " TEXT DEFAULT 'no data'"
                )
            }
        }
    }

    fun open(): GroupAdapter {
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
        getCursor(getDB, groupListList)
        close()
        return groupListList
    }


    //レコードの選択削除
    fun selectDelete(position: String) {
        open()
        db.beginTransaction()                      // トランザクション開始
        try {
            db.delete(TABLE_NAME, "$GP_ID=?", arrayOf(position))
            db.setTransactionSuccessful()          // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()                    // トランザクションの終了
        }
        close()
    }

    //Listの情報取得
    fun getCursor(c: Cursor, groupList: ArrayList<Group>) {
        var group: Group

        groupList.clear()

        if (c.moveToFirst()) {
            do {
                group = Group(
                    c.getInt(c.getColumnIndex(GP_ID)),
                    c.getString(c.getColumnIndex(GP_NAME)),
                    c.getString(c.getColumnIndex(GP_READ)),
                    c.getInt(c.getColumnIndex(GP_BELONG))
                )
                groupList.add(group)          // 取得した要素をgroupListに追加

            } while (c.moveToNext())
        }
        c.close()
    }

    @Throws(IOException::class)
    fun picGroup(group: String, groupList: ArrayList<Group>) {
        open()
        val query = ("SELECT * FROM " + TABLE_NAME +
                " WHERE " + GP_NAME + " like '%" + group + "%' OR "
                + GP_READ + " like '%" + group + "%';")
        val c = db.rawQuery(query, null)
        getCursor(c, groupList)
        close()
    }

    fun sortGroups(sortBy: String, sortType: String, groupList: ArrayList<Group>) {
        open()
        val query = "SELECT * FROM " +
                TABLE_NAME + " ORDER BY " + sortBy + " " + sortType + ";"
        val c = db.rawQuery(query, null)
        getCursor(c, groupList)
        close()
    }

    fun saveGroup(name: String, name_read: String, belongNo: Int) {
        open()
        db.beginTransaction()          // トランザクション開始

        try {
            val values = ContentValues()
            values.put(GP_NAME, name)
            values.put(GP_READ, name_read)
            values.put(GP_BELONG, belongNo)
            db.insert(TABLE_NAME, null, values)

            db.setTransactionSuccessful()      // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()                // トランザクションの終了
        }
        close()
    }

    fun updateGroup(groupId: Int, name: String, name_read: String, belongNo: Int) {
        open()
        val values = ContentValues()
        values.put(GP_NAME, name)
        values.put(GP_READ, name_read)
        values.put(GP_BELONG, belongNo)
        try {
            db.update(TABLE_NAME, values, "$GP_ID=?", arrayOf(groupId.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        close()
    }

    fun updateBelongNo(id: String, newBelongNo: Int) {
        open()
        try {
            val values = ContentValues()
            values.put(GP_BELONG, newBelongNo)
            db.update(TABLE_NAME, values, "$GP_ID=?", arrayOf(id))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        close()
    }

    companion object {
        const val DB_VERSION = 2
        const val TABLE_NAME = "group_info"
        const val GP_ID = "_id"
        const val GP_NAME = "gp_name"
        const val GP_READ = "gp_name_read"
        const val GP_BELONG = "gp_belong"
        lateinit var db: SQLiteDatabase

        const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " ("
                + GP_ID + " INTEGER PRIMARY KEY," + GP_NAME + " TEXT NOT NULL,"
                + GP_READ + " TEXT," + GP_BELONG + " INTEGER" + ");")
    }
}
