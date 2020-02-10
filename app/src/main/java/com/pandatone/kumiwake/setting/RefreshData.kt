package com.pandatone.kumiwake.setting

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.GroupListAdapter
import com.pandatone.kumiwake.adapter.MemberListAdapter
import com.pandatone.kumiwake.member.Group
import com.pandatone.kumiwake.member.Member


object RefreshData {

    @SuppressLint("StaticFieldLeak")
    lateinit var mbDbAdapter: MemberListAdapter
    lateinit var gpDbAdapter: GroupListAdapter

    private val memberList: ArrayList<Member> = ArrayList()
    val groupList: ArrayList<Group> = ArrayList()

    fun refresh(context: Context) {

        mbDbAdapter = MemberListAdapter(memberList,context)
        gpDbAdapter = GroupListAdapter(context)

        //旧グループ確保＆データベース全削除＆新グループ登録
        gpDbAdapter.open()
        val gpc = gpDbAdapter.getDB
        gpDbAdapter.getCursor(gpc, groupList)
        allDelete(1)
        gpDbAdapter.close()
        saveAllGroup()

        //旧メンバー確保＆データベース全削除＆新メンバー登録
        mbDbAdapter.open()
        val mbc = mbDbAdapter.getDB
        mbDbAdapter.getCursor(mbc, memberList,false)
        allDelete(0)
        mbDbAdapter.close()
        saveAllName()

        //グループのメンバー数登録
        updateBelongNo()

        Toast.makeText(context, context.getString(R.string.refresh_completed), Toast.LENGTH_SHORT).show()
    }

    private fun allDelete(code: Int) {

        if (code == 0) {
            MemberListAdapter.db.beginTransaction()                      // トランザクション開始
            try {
                MemberListAdapter.db.delete(MemberListAdapter.TABLE_NAME, null, null)
                MemberListAdapter.db.setTransactionSuccessful()          // トランザクションへコミット
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                MemberListAdapter.db.endTransaction()                    // トランザクションの終了
            }
        } else {
            GroupListAdapter.db.beginTransaction()                      // トランザクション開始
            try {
                GroupListAdapter.db.delete(GroupListAdapter.TABLE_NAME, null, null)
                GroupListAdapter.db.setTransactionSuccessful()          // トランザクションへコミット
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                GroupListAdapter.db.endTransaction()                    // トランザクションの終了
            }
        }
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////Belong更新メソッド/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun convertToNewBelong(member: Member, oldGroupList: ArrayList<Group>): String {
        val belongGroupNames = belongNoToName(member, oldGroupList)
        return belongTextToNo(belongGroupNames)
    }

    private fun belongNoToName(member: Member, oldGroupList: ArrayList<Group>): String {
        val result: String

        val belongText = member.belong
        val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val newBelong = StringBuilder()

        for (belongGroup in belongArray) {
            for (group in oldGroupList) {
                val groupId = group.id.toString()
                if (belongGroup == groupId) {
                    val listName = group.name
                    newBelong.append("$listName,")
                }
            }
        }

        result = if (newBelong.toString() == "") {
            ""
        } else {
            newBelong.substring(0, newBelong.length - 1)
        }

        return result
    }

    //groupのrefresh終わった後呼ぶ
    private fun belongTextToNo(belongText: String): String {
        val belongTextArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val belongNo = StringBuilder()

        for (belongGroup in belongTextArray) {
            for (group in groupList) {
                if (belongGroup == group.name) {
                    val groupId = group.id.toString()
                    belongNo.append("$groupId,")
                }
            }
        }

        return belongNo.toString()
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

}
