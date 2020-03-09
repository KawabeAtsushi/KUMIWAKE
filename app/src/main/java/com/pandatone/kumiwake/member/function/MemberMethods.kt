package com.pandatone.kumiwake.member.function

import android.content.Context
import com.pandatone.kumiwake.adapter.GroupAdapter
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.ui.members.FragmentGroupMain

object MemberMethods {

    //Groupの所属人数データ更新
    fun updateBelongNo(context: Context) {
        val gpAdapter = GroupAdapter(context)
        val members = MemberAdapter(context).getAllMembers()
        for (group in gpAdapter.getAllGroups()) {
            val groupId = group.id.toString()
            var belongNo = 0
            members.forEach { member ->
                val belongArray = member.belong.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val list = ArrayList(listOf<String>(*belongArray))
                if (list.contains(groupId)) {
                    belongNo++
                }
            }
            gpAdapter.updateBelongNo(groupId, belongNo)
        }
        FragmentGroupMain().loadName()
    }

    //memberのAgeを更新
    fun updateAge(context: Context,memberList: ArrayList<Member>,newAge:Int ,define:Boolean) {
        val mbAdapter = MemberAdapter(context)
        memberList.forEach { member ->
            val listId = member.id
            if (define){
                mbAdapter.updateAge(listId.toString(), newAge.toString())
            }else{
                val nowAge = member.age
                mbAdapter.updateAge(listId.toString(), (nowAge+newAge).toString())
            }
        }
    }

}