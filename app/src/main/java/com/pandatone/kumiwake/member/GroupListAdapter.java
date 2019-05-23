package com.pandatone.kumiwake.member;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ArrayAdapter;

import com.pandatone.kumiwake.adapter.GroupNameListAdapter;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by atsushi_2 on 2016/03/20.
 */
public class GroupListAdapter extends ArrayAdapter<GroupListAdapter.Group> {
    public static final String DB_NAME = "kumiwake.db";
    public static final int DB_VERSION = 2;
    public static final String TABLE_NAME = "group_info";
    public static final String GP_ID = "_id";
    public static final String GP_NAME = "gp_name";
    public static final String GP_NAME_READ = "gp_name_read";
    public static final String GP_BELONG = "gp_belong";
    private final Context context;


    protected DatabaseHelper dbHelper;
    public static SQLiteDatabase db;

    public GroupListAdapter(Context context) {
        super(context, 0);
        this.context = context;
        dbHelper = new DatabaseHelper(this.context);
    }

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + GP_ID + " INTEGER PRIMARY KEY," + GP_NAME + " TEXT NOT NULL,"
            + GP_NAME_READ + " TEXT,"+ GP_BELONG + " INTEGER" + ");";


    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, TABLE_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1 && newVersion == 2) {

                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + GP_NAME_READ
                        + " TEXT DEFAULT 'no data'");

            }
        }
    }

    public GroupListAdapter open() {
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
            db.delete(TABLE_NAME, GP_ID + "=?", new String[]{position});
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

    public Cursor getAllNames() {
        return db.query(TABLE_NAME, null,null, null, null, null, null);
    }

    public void getCursor(Cursor c) {
        List<Group> nameList = FragmentGroup.nameList;
        GroupNameListAdapter listAdapter = FragmentGroup.listAdapter;
        Group listItem;

        nameList.clear();
        if (c.moveToFirst()) {
            do {
                listItem = new GroupListAdapter.Group(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getString(3)
                );
                nameList.add(listItem);          // 取得した要素をnameListに追加

            } while (c.moveToNext());
        }
        c.close();
        listAdapter.notifyDataSetChanged();
    }

    public void picGroup(String group, String group_read) throws IOException {

        open();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + GP_NAME + " like '%" + group + "%' OR "
                + GP_NAME_READ + " like '%" + group_read + "%';";
        Cursor c = db.rawQuery(query, null);
        getCursor(c);
        close();
    }

    public void sortGroups(String sortBy, String sortType) {
        open();
        String query = "SELECT * FROM " +
                TABLE_NAME + " ORDER BY " + sortBy + " " + sortType + ";";
        Cursor c = db.rawQuery(query, null);
        getCursor(c);
        close();
    }

    public void saveGroup(String name,String name_read,int belongNo) {

        db.beginTransaction();          // トランザクション開始

        try {
            ContentValues values = new ContentValues();
            values.put(GP_NAME, name);
            values.put(GP_NAME_READ, name_read);
            values.put(GP_BELONG, belongNo);
            db.insert(TABLE_NAME, null, values);

            db.setTransactionSuccessful();      // トランザクションへコミット
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();                // トランザクションの終了
        }

    }

    public void updateGroup(int groupId, String name,String name_read, int belongNo) {
        open();
        ContentValues values = new ContentValues();
        values.put(GP_NAME, name);
        values.put(GP_NAME_READ, name_read);
        values.put(GP_BELONG, belongNo);
        try {
            db.update(TABLE_NAME, values, GP_ID + "=?", new String[]{String.valueOf(groupId)});
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

    public int getMaxId() {
        open();
        Cursor c = db.rawQuery("SELECT MAX(" + GP_ID + ") FROM " + TABLE_NAME, null);
        c.moveToFirst();
        int idMax = c.getInt(0);
        close();
        return idMax;
    }

    public static class Group implements Serializable {
        protected int id;
        protected String group_name;
        protected String group_name_read;
        protected int belong_no;

        public Group(int id,String group_name, int belong_no, String group_name_read) {
            this.id = id;
            this.group_name = group_name;
            this.belong_no = belong_no;
            this.group_name_read = group_name_read;
        }

        public int getId() {
            return id;
        }

        public String getGroup() {
            return group_name;
        }

        public String getGroup_read() {
            return group_name_read;
        }

        public int getBelongNo() {
            return belong_no;
        }

    }
}
