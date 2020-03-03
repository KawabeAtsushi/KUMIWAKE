package com.pandatone.kumiwake

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.member.Member
import java.util.*
import kotlin.collections.ArrayList

object GroupMethods {

    //ID = belongIdのグループに所属するメンバーリストを返す
    fun searchBelong(context: Context, belongId: String): ArrayList<Member> {
        val memberArrayByBelong = ArrayList<Member>()
        val members = MemberAdapter(context).getAllMembers()
        members.forEach { member ->
            val belongText = member.belong
            val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (Arrays.asList<String>(*belongArray).contains(belongId)) {
                memberArrayByBelong.add(Member(member.id, member.name, member.sex, 0, 0, null.toString(), null.toString(), null.toString()))
            }
        }
        return memberArrayByBelong
    }

    //全てのメンバーをグループ(groupIdのグループ)から脱退（グループ削除の際にコール）
    fun deleteBelongInfoAll(context: Context,groupId: Int) {
        val mbAdapter = MemberAdapter(context)
        val members = mbAdapter.getAllMembers()
        mbAdapter.open()
        members.forEach { member ->
            deleteBelongInfo(member, groupId, member.id,mbAdapter)
        }
        mbAdapter.close()
    }

    //メンバー(member)を所属グループ(groupIdのグループ)から脱退
    private fun deleteBelongInfo(member: Member, groupId: Int, listId: Int,mbAdapter:MemberAdapter) {
        val belongText = member.belong
        val belongArray = belongText.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val list = ArrayList(Arrays.asList<String>(*belongArray))
        val hs = HashSet<String>()
        hs.addAll(list)
        list.clear()
        list.addAll(hs)
        if (list.contains(groupId.toString())) {
            list.remove(groupId.toString())
            val newBelong = StringBuilder()
            for (item in list) {
                newBelong.append("$item,")
            }
            mbAdapter.addBelong(listId.toString(), newBelong.toString())
        }
    }

}