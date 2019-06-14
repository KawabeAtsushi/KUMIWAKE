package com.pandatone.kumiwake.adapter

/**
 * Created by atsushi_2 on 2016/03/02.
 */

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.pandatone.kumiwake.member.FragmentMember
import com.pandatone.kumiwake.member.Name
import java.io.IOException

class MemberListAdapter(private val context: Context) : BaseAdapter() {


    private var dbHelper: DatabaseHelper

    val allNames: Cursor
        get() = db.query(TABLE_NAME, null, null, null, null, null, null)

    init {
        dbHelper = DatabaseHelper(this.context)
    }

    override fun getCount(): Int {
        return 0
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View? {
        return null
    }


    // SQLiteOpenHelper
    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion == 1 && newVersion == 2) {

                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + MB_NAME_READ
                        + " TEXT DEFAULT 'ￚ no data ￚ'")

            }
        }
    }


    // Adapter Methods
    fun open(): MemberListAdapter {
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
            db.delete(TABLE_NAME, "$MB_ID=?", arrayOf(position))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        close()
    }

    fun sortNames(sortBy: String, sortType: String) {
        open()
        val query = "SELECT * FROM " +
                TABLE_NAME + " ORDER BY " + sortBy + " " + sortType + ";"
        val c = db.rawQuery(query, null)
        getCursor(c)
        close()
    }

    @Throws(IOException::class)
    fun picName(name: String) {
        open()
        val query = ("SELECT * FROM " + TABLE_NAME +
                " WHERE " + MB_NAME + " like '%" + name + "%' OR "
                + MB_NAME_READ + " like '%" + name + "%';")
        val c = db.rawQuery(query, null)
        getCursor(c)
        close()
    }

    @Throws(IOException::class)
    fun filterName(sex: String, minage: Int, maxage: Int, mingrade: Int, maxgrade: Int, belongNo: String, role: String) {

        open()
        val query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + MB_SEX + " like '%" + sex + "%' AND (" + MB_AGE + " BETWEEN " + minage + " AND " + maxage +
                ") AND (" + MB_GRADE + " BETWEEN " + mingrade + " AND " + maxgrade +
                ") AND (" + MB_BELONG + " like '%," + belongNo + "%' OR " + MB_BELONG + " like '" + belongNo + "%')" +
                " AND " + MB_ROLE + " like '%" + role + "%';"
        val c = db.rawQuery(query, null)
        getCursor(c)
        close()

        NameListAdapter.nowSort = "ID"
    }

    fun getCursor(c: Cursor) {
        val nameList = FragmentMember.nameList
        val listAdp = FragmentMember().listAdp
        var listItem: Name

        nameList.clear()

        if (c.moveToFirst()) {
            do {
                //頭文字帯用要素の追加
                if (c.getString(7) != "ￚ no data ￚ" && !c.getString(7).isEmpty()) {
                    listItem = Name(
                            0, null.toString(),
                            "initial",
                            c.getInt(3),
                            c.getInt(4), null.toString(), null.toString(),
                            c.getString(7).toUpperCase().get(0).toString())
                } else {
                    listItem = Name(
                            0, null.toString(),
                            "initial",
                            c.getInt(3),
                            c.getInt(4), null.toString(), null.toString(),
                            "ￚ no data ￚ")
                }

                nameList.add(listItem)          // 取得した要素をnameListに追加

                listItem = Name(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getInt(3),
                        c.getInt(4),
                        c.getString(5),
                        c.getString(6),
                        c.getString(7))

                nameList.add(listItem)          // 取得した要素をnameListに追加

            } while (c.moveToNext())
        }
        c.close()
        listAdp.notifyDataSetChanged()
    }

    fun addBelong(position: String, newBelong: String) {

        try {
            val values = ContentValues()
            values.put(MB_BELONG, newBelong)
            db.update(TABLE_NAME, values, "$MB_ID=?", arrayOf(position))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun saveName(name: String, sex: String, age: Int, grade: Int, belong: String, role: String, read: String) {

        db.beginTransaction()          // トランザクション開始

        try {
            val values = ContentValues()
            values.put(MB_NAME, name)
            values.put(MB_SEX, sex)
            values.put(MB_AGE, age)
            values.put(MB_GRADE, grade)
            values.put(MB_BELONG, belong)
            values.put(MB_ROLE, role)
            if (read.isEmpty()) values.put(MB_NAME_READ, "ￚ no data ￚ") else values.put(MB_NAME_READ, read)
            db.insert(TABLE_NAME, null, values)

            db.setTransactionSuccessful()      // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()                // トランザクションの終了
        }
    }

    fun updateMember(id: Int, name: String, sex: String, age: Int, grade: Int, belong: String, role: String, read: String) {

        open()
        try {
            val values = ContentValues()
            values.put(MB_NAME, name)
            values.put(MB_SEX, sex)
            values.put(MB_AGE, age)
            values.put(MB_GRADE, grade)
            values.put(MB_BELONG, belong)
            values.put(MB_ROLE, role)
            if (read.isEmpty()) values.put(MB_NAME_READ, "ￚ no data ￚ") else values.put(MB_NAME_READ, read)

            db.update(TABLE_NAME, values, "$MB_ID=?", arrayOf(id.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        close()
    }

    companion object {
        const val DB_NAME = "kumiwake.db"
        const val DB_VERSION = 2
        const val TABLE_NAME = "member_info"
        const val MB_ID = "_id"
        const val MB_NAME = "mb_name"
        const val MB_NAME_READ = "mb_read"
        const val MB_SEX = "mb_sex"
        const val MB_AGE = "mb_age"
        const val MB_GRADE = "mb_grade"
        const val MB_BELONG = "mb_belong"
        const val MB_ROLE = "mb_role"
        lateinit var db: SQLiteDatabase


        const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " ("
                + MB_ID + " INTEGER PRIMARY KEY,"
                + MB_NAME + " TEXT NOT NULL," + MB_SEX + " TEXT NOT NULL,"
                + MB_AGE + " INTEGER," + MB_GRADE + " INTEGER," + MB_BELONG + " TEXT," + MB_ROLE + " TEXT,"
                + MB_NAME_READ + " TEXT"
                + ");")
    }

}