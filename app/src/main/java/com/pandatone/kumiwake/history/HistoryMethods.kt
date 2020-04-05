package com.pandatone.kumiwake.history

import android.content.Context
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.member.function.Member

object HistoryMethods {

    fun resultArrayToString(resultArray: ArrayList<ArrayList<Member>>): String {
        //",/"がグループ区切りの合図
        var resultString = ","
        resultArray.forEach { group ->
            resultString += "/"
            group.forEach { member ->
                resultString += (member.id.toString() + ",")
            }
        }
        return resultString
    }

    fun stringToResultArray(context: Context, resultStr: String): ArrayList<ArrayList<Member>> {
        val groups = resultStr.split(",/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        groups.dropLast(1) //最後は空なので削除
        val resultArray = ArrayList<ArrayList<Member>>(groups.size)
        val memberList = MemberAdapter(context).getAllMembers()
        var groupIndex = 0
        groups.forEach { group ->
            val resultNoArray = group.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            resultNoArray.forEach { id ->
                resultArray[groupIndex].add(pickMember(context, id, memberList))
            }
            groupIndex++
        }
        return resultArray
    }

    private fun pickMember(context: Context, id: String, memberList: ArrayList<Member>): Member {
        val noMember = Member(-1, context.getString(R.string.deleted_mamber), "none", -1, -1, "", "", "")
        val member = memberList.find { it.id.toString() == id }
        member?.let { it ->
            // memberがnullでないときだけ実行
            return it
        } ?: run {
            // memberがnullのときだけ実行
            return noMember
        }
    }
}