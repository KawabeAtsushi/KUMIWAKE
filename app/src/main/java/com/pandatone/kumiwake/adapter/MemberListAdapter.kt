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
import java.util.*


class MemberListAdapter(private val context: Context) : BaseAdapter() {


    private var dbHelper: DatabaseHelper

    val getDB: Cursor
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

                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + MB_READ
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
        db.beginTransaction()                      // トランザクション開始
        try {
            db.delete(TABLE_NAME, "$MB_ID=?", arrayOf(position))
            db.setTransactionSuccessful()          // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()                    // トランザクションの終了
        }
        close()
    }

    fun sortNames(sortBy: String, sortType: String) {

        open()
        val query = "SELECT * FROM " +
                TABLE_NAME + " ORDER BY " + sortBy + " " + sortType + ";"
        val c = db.rawQuery(query, null)
        getCursor(c, FragmentMember.nameList)
        close()

    }

    @Throws(IOException::class)
    fun picName(name: String) {
        open()
        val query = ("SELECT * FROM " + TABLE_NAME +
                " WHERE " + MB_NAME + " like '%" + name + "%' OR "
                + MB_READ + " like '%" + name + "%';")
        val c = db.rawQuery(query, null)
        getCursor(c, FragmentMember.nameList)
        close()

    }

    @Throws(IOException::class)
    fun filterName(sex: String, minage: Int, maxage: Int, belongNo: String) {

        open()
        val query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + MB_SEX + " like '" + sex + "%' AND (" + MB_AGE + " BETWEEN " + minage + " AND " + maxage +
                ") AND (" + MB_BELONG + " like '" + belongNo + "%' OR " + MB_BELONG + " like '%," + belongNo + "%');" //BelongIdのマッチング式OR (e.g),1,2,3,4
        val c = db.rawQuery(query, null)
        getCursor(c, FragmentMember.nameList)
        close()
    }

    fun getCursor(c: Cursor, nameList: ArrayList<Name>) {
        var listItem: Name

        nameList.clear()

        if (c.moveToFirst()) {
            do {
                val read = c.getString(7)
                //頭文字帯用要素の追加
                listItem = if (read != "ￚ no data ￚ" && read.isNotEmpty()) {
                    Name(
                            0, null.toString(),
                            "initial",
                            c.getInt(3),
                            c.getInt(4),
                            null.toString(),
                            null.toString(),
                            read.toUpperCase()[0].toString())
                } else {
                    Name(
                            0, null.toString(),
                            "initial",
                            c.getInt(3),
                            c.getInt(4),
                            null.toString(),
                            null.toString(),
                            "ￚ no data ￚ")
                }

                nameList.add(listItem)          // 取得した要素をnameListに追加

                //member rowを追加
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
    }

    fun addBelong(id: String, newBelong: String) {
        try {
            val values = ContentValues()
            values.put(MB_BELONG, newBelong)
            db.update(TABLE_NAME, values, "$MB_ID=?", arrayOf(id))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun saveName(name: String, sex: String, age: Int, belong: String, read: String) {
        open()
        db.beginTransaction()          // トランザクション開始

        try {
            val values = ContentValues()
            values.put(MB_NAME, name)
            values.put(MB_SEX, sex)
            values.put(MB_AGE, age)
            values.put(MB_GRADE, 0) //grade column is being deprecated
            values.put(MB_BELONG, belong)
            values.put(MB_ROLE, "") //role column is being deprecated
            if (read.isEmpty()) values.put(MB_READ, "ￚ no data ￚ") else values.put(MB_READ, read)
            db.insert(TABLE_NAME, null, values)

            db.setTransactionSuccessful()      // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()                // トランザクションの終了
        }
        close()
    }

    fun updateMember(id: Int, name: String, sex: String, age: Int, belong: String, read: String) {

        open()
        try {
            val values = ContentValues()
            values.put(MB_NAME, name)
            values.put(MB_SEX, sex)
            values.put(MB_AGE, age)
            values.put(MB_GRADE, 0)  //grade column is being deprecated
            values.put(MB_BELONG, belong)
            values.put(MB_ROLE, "")   //role column is being deprecated
            if (read.isEmpty()) values.put(MB_READ, "ￚ no data ￚ") else values.put(MB_READ, read)

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
        const val MB_READ = "mb_read"
        const val MB_SEX = "mb_sex"
        const val MB_AGE = "mb_age"
        const val MB_GRADE = "mb_grade"  //deprecated
        const val MB_BELONG = "mb_belong"
        const val MB_ROLE = "mb_role" //deprecated
        lateinit var db: SQLiteDatabase


        const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " ("
                + MB_ID + " INTEGER PRIMARY KEY,"
                + MB_NAME + " TEXT NOT NULL," + MB_SEX + " TEXT NOT NULL,"
                + MB_AGE + " INTEGER," + MB_GRADE + " INTEGER," + MB_BELONG + " TEXT," + MB_ROLE + " TEXT,"
                + MB_READ + " TEXT"
                + ");")
    }

}