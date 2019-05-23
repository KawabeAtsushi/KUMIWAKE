package com.pandatone.kumiwake.adapter;

/**
 * Created by atsushi_2 on 2016/03/02.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pandatone.kumiwake.member.FragmentMember;
import com.pandatone.kumiwake.member.Name;

import java.io.IOException;
import java.util.List;

public class MemberListAdapter extends BaseAdapter {
    public static final String DB_NAME = "kumiwake.db";
    public static final int DB_VERSION = 2;
    public static final String TABLE_NAME = "member_info";
    public static final String MB_ID = "_id";
    public static final String MB_NAME = "mb_name";
    public static final String MB_NAME_READ = "mb_name_read";
    public static final String MB_SEX = "mb_sex";
    public static final String MB_AGE = "mb_age";
    public static final String MB_GRADE = "mb_grade";
    public static final String MB_BELONG = "mb_belong";
    public static final String MB_ROLE = "mb_role";
    private final Context context;


    protected DatabaseHelper dbHelper;
    public static SQLiteDatabase db;

    public MemberListAdapter(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(this.context);
    }


    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + MB_ID + " INTEGER PRIMARY KEY,"
            + MB_NAME + " TEXT NOT NULL," + MB_SEX + " TEXT NOT NULL,"
            + MB_AGE + " INTEGER," + MB_GRADE + " INTEGER," + MB_BELONG + " TEXT," + MB_ROLE + " TEXT,"
            + MB_NAME_READ + " TEXT"
            + ");";

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }


    // SQLiteOpenHelper
    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1 && newVersion == 2) {

                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + MB_NAME_READ
                        + " TEXT DEFAULT 'ￚ no data ￚ'");

            }
        }
    }


    // Adapter Methods
    public MemberListAdapter open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    //レコードの選択削除
    public void selectDelete(String position) {
        open();
        try {
            db.delete(TABLE_NAME, MB_ID + "=?", new String[]{position});
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

    public Cursor getAllNames() {
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }

    public void sortNames(String sortBy, String sortType) {
        open();
        String query = "SELECT * FROM " +
                TABLE_NAME + " ORDER BY " + sortBy + " " + sortType + ";";
        Cursor c = db.rawQuery(query, null);
        getCursor(c);
        close();
    }

    public void picName(String name) throws IOException {
        open();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + MB_NAME + " like '%" + name + "%' OR "
                + MB_NAME_READ + " like '%" + name + "%';";
        Cursor c = db.rawQuery(query, null);
        getCursor(c);
        close();
    }

    public void filterName(String sex, int minage, int maxage, int mingrade, int maxgrade, String belongNo, String role) throws IOException {

        open();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + MB_SEX + " like '%" + sex + "%' AND (" + MB_AGE + " BETWEEN " + minage + " AND " + maxage +
                ") AND (" + MB_GRADE + " BETWEEN " + mingrade + " AND " + maxgrade +
                ") AND (" + MB_BELONG + " like '%," + belongNo + "%' OR " + MB_BELONG + " like '" + belongNo + "%')" +
                " AND " + MB_ROLE + " like '%" + role + "%';";
        Cursor c = db.rawQuery(query, null);
        getCursor(c);
        close();

        NameListAdapter.nowSort="ID";
    }

    public void getCursor(Cursor c) {
        List<Name> nameList = FragmentMember.nameList;
        NameListAdapter listAdapter = FragmentMember.listAdapter;
        Name listItem;

        nameList.clear();

        if (c.moveToFirst()) {
            do {
                //頭文字帯用要素の追加
                if (!c.getString(7).equals("ￚ no data ￚ") && !c.getString(7).isEmpty()) {
                    listItem = new Name(
                            0,
                            null,
                            "initial",
                            c.getInt(3),
                            c.getInt(4),
                            null,
                            null,
                            String.valueOf(c.getString(7).toUpperCase().charAt(0)));
                } else {
                    listItem = new Name(
                            0,
                            null,
                            "initial",
                            c.getInt(3),
                            c.getInt(4),
                            null,
                            null,
                            "ￚ no data ￚ");
                }

                nameList.add(listItem);          // 取得した要素をnameListに追加

                listItem = new Name(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getInt(3),
                        c.getInt(4),
                        c.getString(5),
                        c.getString(6),
                        c.getString(7));

                nameList.add(listItem);          // 取得した要素をnameListに追加

            } while (c.moveToNext());
        }
        c.close();
        listAdapter.notifyDataSetChanged();
    }

    public void addBelong(String position, String newBelong) {

        try {
            ContentValues values = new ContentValues();
            values.put(MB_BELONG, newBelong);
            db.update(TABLE_NAME, values, MB_ID + "=?", new String[]{position});
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveName(String name, String sex, int age, int grade, String belong, String role, String name_read) {

        db.beginTransaction();          // トランザクション開始

        if (name_read.isEmpty()) {
            name_read = "ￚ no data ￚ";
        }

        try {
            ContentValues values = new ContentValues();
            values.put(MB_NAME, name);
            values.put(MB_SEX, sex);
            values.put(MB_AGE, age);
            values.put(MB_GRADE, grade);
            values.put(MB_BELONG, belong);
            values.put(MB_ROLE, role);
            values.put(MB_NAME_READ, name_read);
            db.insert(TABLE_NAME, null, values);

            db.setTransactionSuccessful();      // トランザクションへコミット
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();                // トランザクションの終了
        }
    }

    public void updateMember(int id, String name, String sex, int age, int grade, String belong, String role, String name_read) {

        if (name_read.isEmpty()) {
            name_read = "ￚ no data ￚ";
        }

        open();
        try {
            ContentValues values = new ContentValues();
            values.put(MB_NAME, name);
            values.put(MB_SEX, sex);
            values.put(MB_AGE, age);
            values.put(MB_GRADE, grade);
            values.put(MB_BELONG, belong);
            values.put(MB_ROLE, role);
            values.put(MB_NAME_READ, name_read);

            db.update(TABLE_NAME, values, MB_ID + "=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

}