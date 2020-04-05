package com.pandatone.kumiwake.setting

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.widget.Toast
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.member.function.Group
import com.pandatone.kumiwake.member.function.Member
import com.pandatone.kumiwake.member.function.MemberClick
import com.pandatone.kumiwake.member.function.MemberMethods


object RefreshData {

    @SuppressLint("StaticFieldLeak")
    private lateinit var mbDbAdapter: MemberAdapter
    private lateinit var gpDbAdapter: GroupAdapter

    private var memberList: ArrayList<Member> = ArrayList()
    private var groupList: ArrayList<Group> = ArrayList()

    fun refresh(context: Context) {

        mbDbAdapter = MemberAdapter(context)
        gpDbAdapter = GroupAdapter(context)

        //旧グループ確保＆データベース全削除＆新グループ登録
        groupList = gpDbAdapter.getAllGroups()
        deleteGroups()
        saveAllGroup()

        //旧メンバー確保＆データベース全削除＆新メンバー登録
        memberList = mbDbAdapter.getAllMembers()
        deleteMembers()
        saveAllName()

        //グループのメンバー数登録
        updateBelongNo()

        Toast.makeText(context, context.getString(R.string.refresh_completed), Toast.LENGTH_SHORT).show()
    }

    private fun deleteGroups() {
        gpDbAdapter.open()
        GroupAdapter.db.beginTransaction()                      // トランザクション開始
        try {
            GroupAdapter.db.delete(GroupAdapter.TABLE_NAME, null, null)
            GroupAdapter.db.setTransactionSuccessful()          // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            GroupAdapter.db.endTransaction()                    // トランザクションの終了
        }
        gpDbAdapter.close()
    }

    private fun deleteMembers() {
        mbDbAdapter.open()
        MemberAdapter.db.beginTransaction()                      // トランザクション開始
        try {
            MemberAdapter.db.delete(MemberAdapter.TABLE_NAME, null, null)
            MemberAdapter.db.setTransactionSuccessful()          // トランザクションへコミット
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            MemberAdapter.db.endTransaction()                    // トランザクションの終了
        }
        mbDbAdapter.close()
    }

    private fun saveAllName() {
        val oldGroupList: ArrayList<Group> = ArrayList(groupList) //DeepCopyコピー渡し

        //groupListを最新の状態に更新
        gpDbAdapter.open()
        val gpc = gpDbAdapter.getDB
        groupList.clear()
        gpDbAdapter.getCursor(gpc, groupList)
        gpDbAdapter.close()

        for (member in memberList) {
            val belong = convertToNewBelong(member, oldGroupList)
            mbDbAdapter.saveName(member.name, member.sex, member.age, belong, member.read)
        }
    }

    private fun saveAllGroup() {
        for (group in groupList) {
            gpDbAdapter.saveGroup(group.name, group.name, 0)
        }
    }

    //前のデータのbelongを整頓する
    private fun convertToNewBelong(member: Member, oldGroupList: ArrayList<Group>): String {
        val belongGroupNames = MemberClick.viewBelong(member, oldGroupList)
        return MemberMethods.belongConvertToNo(belongGroupNames, groupList)
    }

    //グループの所属メンバー数更新
    private fun updateBelongNo() {
        for (group in groupList) {
            val belongCount = countBelongedMember(group)
            gpDbAdapter.updateBelongNo(group.id.toString(), belongCount)
        }
    }

    private fun countBelongedMember(group: Group): Int {
        var belongNo = 0

        for (member in memberList) {
            val belongText = member.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (belongArray.contains(group.id.toString()))
                belongNo++
        }

        return belongNo
    }

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
                        -1)

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
