package com.pandatone.kumiwake.history

import android.app.Activity
import android.content.Context
import android.text.format.DateFormat
import com.pandatone.kumiwake.R
import com.pandatone.kumiwake.adapter.MemberAdapter
import com.pandatone.kumiwake.member.function.Member
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object HistoryMethods {

    private fun resultArrayToString(resultArray: ArrayList<ArrayList<Member>>): String {
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
        val groups = resultStr.split(",/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().toCollection(ArrayList())
        groups.removeAll { it.count() == 0 } //空配列を削除
        val resultArray = ArrayList<ArrayList<Member>>()
        val memberList = MemberAdapter(context).getAllMembers()
        groups.forEach { group ->
            val resultNoArray = group.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val result = ArrayList<Member>()
            resultNoArray.forEach { id ->
                result.add(pickMember(context, id, memberList))
            }
            resultArray.add(result)
        }
        return resultArray
    }

    private fun pickMember(context: Context, id: String, memberList: ArrayList<Member>): Member {
        val noMember = Member(-1, context.getString(R.string.deleted_mamber), "none", -1, "", "", -1)
        val member = memberList.find { it.id.toString() == id }
        member?.let { it ->
            // memberがnullでないときだけ実行
            return it
        } ?: run {
            // memberがnullのときだけ実行
            return noMember
        }
    }

    fun saveResultToHistory(context: Context, resultArray: ArrayList<ArrayList<Member>>, mode: Int, again: Boolean) {
        val hsAdapter = HistoryAdapter(context)
        val resultStr = resultArrayToString(resultArray)
        if (!again) {
            hsAdapter.saveHistory(resultStr, mode, 0)
        } else {
            hsAdapter.changeHistory(resultStr, mode, 0)
        }
    }

    //日付の表記を適切にローカライズ
    fun changeDateFormat(dateStr: String): String {
        var name = dateStr
        if (dateStr.length == 19) { //タイムスタンプなら
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dt = df.parse(dateStr)
            val locale = Locale.getDefault()
            val format = DateFormat.getBestDateTimePattern(locale, "yyyyMMMdHHm")
            val dateFormat = SimpleDateFormat(format, locale)
            name = dateFormat.format(dt)
        }
        return name
    }

    //ソート
    var sortType = "ASC"
    fun historySort(activity: Activity, historyList: ArrayList<History>, listAdp: HistoryFragmentViewAdapter) {
        val hsAdapter = HistoryAdapter(activity)
        if (sortType == "ASC") {
            sortType = "DESC"
        } else {
            sortType = "ASC"
        }
        hsAdapter.sortGroups(sortType, historyList)
        listAdp.notifyDataSetChanged() //loadName()を呼ばない！
    }


}