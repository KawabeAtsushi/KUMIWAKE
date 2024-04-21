package com.pandatone.kumiwake.setting

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.member.function.Member


object RefreshData {

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////Migrate New Table (SQLiteの更新で呼ぶメソッド。テーブルを作り替える)/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getOldMembers(db: SQLiteDatabase): ArrayList<Member> {
        val memberList: ArrayList<Member> = ArrayList()
        val c = db.query(MemberAdapter.OLD_TABLE_NAME, null, null, null, null, null, null)
        if (c.moveToFirst()) {
            do {
                //member rowを追加
                val member = Member(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getInt(3),
                    c.getString(5),
                    c.getString(7),
                    -1
                )

                memberList.add(member)          // 取得した要素をnameListに追加

            } while (c.moveToNext())
        }
        c.close()
        return memberList
    }

    fun migrateToNew(db: SQLiteDatabase, member: Member) {
        db.beginTransaction()          // トランザクション開始
        try {
            val values = ContentValues()
            values.put(MemberAdapter.MB_NAME, member.name)
            values.put(MemberAdapter.MB_SEX, member.sex)
            values.put(MemberAdapter.MB_AGE, member.age)
            values.put(MemberAdapter.MB_BELONG, member.belong)
            values.put(MemberAdapter.MB_READ, member.read)
            db.insert(MemberAdapter.TABLE_NAME, null, values)
            db.setTransactionSuccessful()      // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()                // トランザクションの終了
        }
    }


}
