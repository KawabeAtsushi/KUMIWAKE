package com.pandatone.kumiwake.adapter

/**
 * Created by atsushi_2 on 2016/03/02.
 */

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.pandatone.kumiwake.StatusHolder
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.setting.RefreshData
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MemberAdapter(val context: Context) : BaseAdapter() {


    private var dbHelper: DatabaseHelper

    //DB取得
    val getDB: Cursor
        get() = db.query(TABLE_NAME, null, null, null, null, null, null)

    init {
        dbHelper = DatabaseHelper(this.context)
    }

    //全メンバー数を取得
    override fun getCount(): Int {
        val memberList: ArrayList<Member> = ArrayList()
        open()
        getCursor(getDB, memberList, false)
        close()
        return memberList.size
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
    private class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

            if (oldVersion < 3) {
                //テーブル作り替え(columnをdropできないため)
                //https://phicdy.hatenablog.com/entry/delete-android-sqlite-table
                Toast.makeText(context, "Database Upgraded Ver.$oldVersion to $newVersion", Toast.LENGTH_LONG).show()
                //前のテーブルからデータを取得
                val escapeMembers = RefreshData.getOldMembers(db)
                //前のテーブルを削除
                db.execSQL("DROP TABLE $OLD_TABLE_NAME;")
                //新しいテーブルを作成
                db.execSQL(CREATE_TABLE)
                ////前のテーブルからデータを移行
                escapeMembers.forEach { member ->
                    RefreshData.migrateToNew(db, member)
                }
            }
        }
    }


    // Adapter Methods
    fun open(): MemberAdapter {
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        dbHelper.close()
    }

    //最新のメンバーを取得
    val newMember: Member
        @SuppressLint("Recycle")
        get() {
            open()
            val c = db.rawQuery("SELECT * FROM $TABLE_NAME" +
                    " WHERE $MB_ID=(SELECT MAX($MB_ID) FROM $TABLE_NAME);", null)
            c.moveToFirst()
            val member = Member(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getInt(3),
                    c.getString(4),
                    c.getString(5),
                    -1)
            close()
            return member
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

    //ソート
    fun sortNames(sortBy: String, sortType: String, memberList: ArrayList<Member>) {
        open()
        val query = "SELECT * FROM " +
                TABLE_NAME + " ORDER BY " + sortBy + " " + sortType + ";"
        val c = db.rawQuery(query, null)
        getCursor(c, memberList, true)
        close()
    }

    @Throws(IOException::class)
    fun picName(name: String, memberList: ArrayList<Member>) {
        open()
        val query = ("SELECT * FROM " + TABLE_NAME +
                " WHERE " + MB_NAME + " like '%" + name + "%' OR "
                + MB_READ + " like '%" + name + "%';")
        val c = db.rawQuery(query, null)
        getCursor(c, memberList, true)
        close()
    }

    @Throws(IOException::class)
    fun filterName(sex: String, minAge: Int, maxAge: Int, belongNo: String, memberList: ArrayList<Member>) {
        open()
        val query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + MB_SEX + " like '" + sex + "%' AND (" + MB_AGE + " BETWEEN " + minAge + " AND " + maxAge +
                ") AND (" + MB_BELONG + " like '" + belongNo + "%' OR " + MB_BELONG + " like '%," + belongNo + "%');" //BelongIdのマッチング式OR (e.g),1,2,3,4
        val c = db.rawQuery(query, null)
        getCursor(c, memberList, true)
        close()
    }

    fun getCursor(c: Cursor, memberList: ArrayList<Member>, addInit: Boolean) {
        var member: Member

        memberList.clear()

        if (c.moveToFirst()) {
            do {
                if (addInit) {
                    val read = c.getString(5)
                    //頭文字帯用要素の追加
                    member = if (read != "ￚ no data ￚ" && read.isNotEmpty()) {
                        Member(
                                0,
                                null.toString(),
                                StatusHolder.index,
                                c.getInt(3),
                                null.toString(),
                                read.toUpperCase(Locale.getDefault())[0].toString(),
                                -1)
                    } else {
                        Member(
                                0,
                                null.toString(),
                                StatusHolder.index,
                                c.getInt(3),
                                null.toString(),
                                "ￚ no data ￚ",
                                -1)
                    }
                    memberList.add(member)          // 取得した要素をnameListに追加
                }

                //member rowを追加
                member = Member(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getInt(3),
                        c.getString(4),
                        c.getString(5),
                        -1)

                memberList.add(member)          // 取得した要素をnameListに追加

            } while (c.moveToNext())
        }
        c.close()
    }

    fun getAllMembers(): ArrayList<Member> {
        val memberList: ArrayList<Member> = ArrayList()
        open()
        getCursor(getDB, memberList, false)
        close()
        return memberList
    }

    //Belongの更新
    fun updateBelong(id: String, newBelong: String) {
        open()
        val values = ContentValues()
        values.put(MB_BELONG, newBelong)
        db.update(TABLE_NAME, values, "$MB_ID=?", arrayOf(id))
        close()
    }

    //Ageの更新
    fun updateAge(id: String, newAge: String) {
        open()
        val values = ContentValues()
        values.put(MB_AGE, newAge)
        db.update(TABLE_NAME, values, "$MB_ID=?", arrayOf(id))
        close()
    }

    fun saveName(name: String, sex: String, age: Int, belong: String, read: String) {
        open()
        db.beginTransaction()          // トランザクション開始
        try {
            val values = ContentValues()
            values.put(MB_NAME, name)
            values.put(MB_SEX, sex)
            values.put(MB_AGE, age)
            values.put(MB_BELONG, belong)
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
            values.put(MB_BELONG, belong)
            if (read.isEmpty()) values.put(MB_READ, "ￚ no data ￚ") else values.put(MB_READ, read)

            db.update(TABLE_NAME, values, "$MB_ID=?", arrayOf(id.toString()))

        } catch (e: Exception) {
            e.printStackTrace()
        }

        close()
    }

    companion object {
        const val DB_NAME = "kumiwake.db"
        const val DB_VERSION = 3
        const val TABLE_NAME = "member_table$DB_VERSION"
        const val MB_ID = "_id"
        const val MB_NAME = "mb_name"
        const val MB_READ = "mb_read"
        const val MB_SEX = "mb_sex"
        const val MB_AGE = "mb_age"
        const val MB_BELONG = "mb_belong"
        lateinit var db: SQLiteDatabase

        const val OLD_TABLE_NAME = "member_info"

        const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + " ("
                + MB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MB_NAME + " TEXT NOT NULL," + MB_SEX + " TEXT NOT NULL,"
                + MB_AGE + " INTEGER," + MB_BELONG + " TEXT," + MB_READ + " TEXT" + ");")
    }

    /*
    getColumnIndexがillegalStateException吐くので一旦は番号指定(Groupは今のところ大丈夫なのでそのまま)
    Log.d("MB_ID",c.getColumnIndex(MB_ID).toString()) -> 0
    Log.d("MB_NAME",c.getColumnIndex(MB_NAME).toString()) -> 1
    Log.d("MB_SEX",c.getColumnIndex(MB_SEX).toString()) -> 2
    Log.d("MB_AGE",c.getColumnIndex(MB_AGE).toString()) -> 3
    Log.d("MB_GRADE",c.getColumnIndex(MB_GRADE).toString()) -> 4
    Log.d("MB_BELONG",c.getColumnIndex(MB_BELONG).toString()) -> 5
    Log.d("MB_ROLE",c.getColumnIndex(MB_ROLE).toString()) -> 6
    Log.d("MB_READ",c.getColumnIndex(MB_READ).toString()) -> 7
     */

}