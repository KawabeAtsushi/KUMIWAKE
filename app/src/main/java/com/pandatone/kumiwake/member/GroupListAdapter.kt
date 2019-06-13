package com.pandatone.kumiwake.member

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.ArrayAdapter
import java.io.IOException
import java.io.Serializable

/**
 * Created by atsushi_2 on 2016/03/20.
 */
class GroupListAdapter(private val context: Context) : ArrayAdapter<GroupListAdapter.Group>(context, 0) {


    protected var dbHelper: DatabaseHelper

    val allNames: Cursor
        get() = db.query(TABLE_NAME, null, null, null, null, null, null)

    val maxId: Int
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


    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, TABLE_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion == 1 && newVersion == 2) {

                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + GP_NAME_READ
                        + " TEXT DEFAULT 'no data'")

            }
        }
    }

    fun open(): GroupListAdapter {
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        dbHelper.close()
    }


    //レコードの選択削除
    fun selectDelete(position: String) {
        open()
        try {
            db.delete(TABLE_NAME, "$GP_ID=?", arrayOf(position))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        close()
    }

    fun getCursor(c: Cursor) {
        val nameList = FragmentGroup.nameList
        val listAdapter = FragmentGroup.listAdapter
        var listItem: Group

        nameList.clear()
        if (c.moveToFirst()) {
            do {
                listItem = GroupListAdapter.Group(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getString(3)
                )
                nameList.add(listItem)          // 取得した要素をnameListに追加

            } while (c.moveToNext())
        }
        c.close()
        listAdapter.notifyDataSetChanged()
    }

    @Throws(IOException::class)
    fun picGroup(group: String, group_read: String) {

        open()
        val query = ("SELECT * FROM " + TABLE_NAME +
                " WHERE " + GP_NAME + " like '%" + group + "%' OR "
                + GP_NAME_READ + " like '%" + group_read + "%';")
        val c = db.rawQuery(query, null)
        getCursor(c)
        close()
    }

    fun sortGroups(sortBy: String, sortType: String) {
        open()
        val query = "SELECT * FROM " +
                TABLE_NAME + " ORDER BY " + sortBy + " " + sortType + ";"
        val c = db.rawQuery(query, null)
        getCursor(c)
        close()
    }

    fun saveGroup(name: String, name_read: String, belongNo: Int) {

        db.beginTransaction()          // トランザクション開始

        try {
            val values = ContentValues()
            values.put(GP_NAME, name)
            values.put(GP_NAME_READ, name_read)
            values.put(GP_BELONG, belongNo)
            db.insert(TABLE_NAME, null, values)

            db.setTransactionSuccessful()      // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()                // トランザクションの終了
        }

    }

    fun updateGroup(groupId: Int, name: String, name_read: String, belongNo: Int) {
        open()
        val values = ContentValues()
        values.put(GP_NAME, name)
        values.put(GP_NAME_READ, name_read)
        values.put(GP_BELONG, belongNo)
        try {
            db.update(TABLE_NAME, values, "$GP_ID=?", arrayOf(groupId.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        close()
    }

    class Group(id: Int, group_name: String, belong_no: Int, group_name_read: String) : Serializable {
        var id: Int = 0
            protected set
        var group: String
            protected set
        var group_read: String
            protected set
        var belongNo: Int = 0
            protected set

        init {
            this.id = id
            this.group = group_name
            this.belongNo = belong_no
            this.group_read = group_name_read
        }

    }

    companion object {
        val DB_NAME = "kumiwake.db"
        val DB_VERSION = 2
        val TABLE_NAME = "group_info"
        val GP_ID = "_id"
        val GP_NAME = "gp_name"
        val GP_NAME_READ = "gp_name_read"
        val GP_BELONG = "gp_belong"
        var db: SQLiteDatabase

        val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " ("
                + GP_ID + " INTEGER PRIMARY KEY," + GP_NAME + " TEXT NOT NULL,"
                + GP_NAME_READ + " TEXT," + GP_BELONG + " INTEGER" + ");")
    }
}
